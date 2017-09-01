package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.PrintOrderItem
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.PrintOrderService
import com.unicolour.joyspace.service.PrintStationService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import javax.transaction.Transactional
import kotlin.experimental.and
import java.io.InputStreamReader
import java.io.StringReader
import java.net.URL
import javax.annotation.PostConstruct
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller


@Service
open class PrintOrderServiceImpl : PrintOrderService {
    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationService: PrintStationService

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var printOrderItemDao: PrintOrderItemDao

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Value("\${com.unicolour.wxAppId}")
    lateinit var wxAppId: String

    @Value("\${com.unicolour.wxMchId}")  //商户号
    lateinit var wxMchId: String

    @Value("\${com.unicolour.wxPayKey}")
    lateinit var wxPayKey: String

    private lateinit var wxUnifyOrderResultUnmarshaller: Unmarshaller
    private lateinit var wxPayNotifyUnmarshaller: Unmarshaller

    @PostConstruct
    fun initialize() {
        wxUnifyOrderResultUnmarshaller = JAXBContext.newInstance(WxUnifyOrderResult::class.java).createUnmarshaller()
        wxPayNotifyUnmarshaller = JAXBContext.newInstance(WxPayNotify::class.java).createUnmarshaller()
    }

    @Transactional
    override fun createOrder(orderInput: OrderInput): PrintOrder {
        val session = userLoginSessionDao.findOne(orderInput.sessionId)

        if (session == null) {
            throw ProcessException(1, "用户未登录")
        }

        val printStation = printStationDao.findOne(orderInput.printStationId)

        if (printStation == null) {
            throw ProcessException(2, "没有找到指定的自助机")
        }

        val order = PrintOrder()
        order.orderNo = createOrderNo()
        order.companyId = printStation.companyId
        order.createTime = Calendar.getInstance()
        order.updateTime = order.createTime
        order.printStationId = orderInput.printStationId
        order.userId = session.userId
        order.state = PrintOrderState.CREATED.value

        printOrderDao.save(order)

        for (orderItem in orderInput.orderItems) {
            val newOrderItem = PrintOrderItem()
            newOrderItem.copies = orderItem.copies
            newOrderItem.printOrder = order
            newOrderItem.product = productDao.findOne(orderItem.productId)

            printOrderItemDao.save(newOrderItem)

            val userImgFile = userImageFileDao.findOne(orderItem.imageFileId)
            userImgFile.orderItem = newOrderItem
            userImageFileDao.save(userImgFile)
        }

        return order
    }

    private fun createOrderNo(): String {
        val dateTime = SimpleDateFormat("yyyyMMdd").format(Date())
        val randomStr = BigInteger(4 * 8, secureRandom).toString(36).toUpperCase()
        var orderNo:String
        do {
            orderNo = "$dateTime-$randomStr"
        } while (printOrderDao.existsByOrderNo(orderNo))

        return orderNo
    }

    override fun getImageFilesDataFetcher(): DataFetcher<Array<UserImageFile>> {
        return DataFetcher { env ->
            val printOrderItem = env.getSource<PrintOrderItem>()
            printOrderItem.userImageFiles.toTypedArray()
        }
    }

    override fun getPrintOrderDataFetcher(): DataFetcher<PrintOrder?> {
        return DataFetcher { env ->
            val printStationId = env.getArgument<Int>("printStationId")
            val idAfter = env.getArgument<Int>("idAfter")

            printOrderDao.findFirstByPrintStationIdAndStateAndIdAfter(printStationId, PrintOrderState.PAYED.value, idAfter)
        }
    }

    override fun getUpdateOrderStateDataFetcher(state: PrintOrderState): DataFetcher<GraphQLRequestResult> {
        return DataFetcher { env ->
            val printStationId = env.getArgument<Int>("printStationId")
            val printOrderId = env.getArgument<Int>("printOrderId")

            val order = printOrderDao.findOne(printOrderId)
            if (order != null) {
                order.state = state.value
                printOrderDao.save(order)
                GraphQLRequestResult(ResultCode.SUCCESS)
            }
            else {
                GraphQLRequestResult(ResultCode.PRINTER_ORDER_NOT_FOUND)
            }
        }
    }

    override fun startPayment(orderId: Int, baseUrl:String): WxPayParams {
        val order = printOrderDao.findOne(orderId)
        val printStation = printStationDao.findOne(order.printStationId)
        val priceMap = printStationService.getPriceMap(printStation)

        val user = userDao.findOne(order.userId)
        val openId: String = user?.wxOpenId ?: ""

        var nonceStr: String = BigInteger(32 * 8, secureRandom).toString(36).toUpperCase()
        if (nonceStr.length > 32) {
            nonceStr = nonceStr.substring(0, 32)
        }
        val notifyUrl = "$baseUrl/wxpay/notify"
        val ipAddress: String = java.net.InetAddress.getByName(URL(baseUrl).host).hostAddress

        val orderItems = printOrderItemDao.findByPrintOrderId(order.id)
        var totalFee = 0
        for (printOrderItem in orderItems) {
            val orderItemFee:Int = priceMap.getOrElse(printOrderItem.product.id, { printOrderItem.product.defaultPrice })
            totalFee += orderItemFee * printOrderItem.copies
        }

        val requestBody = getPaymentRequestParams(TreeMap(hashMapOf<String, String>(
                "appid" to wxAppId,
                "body" to "优利绚彩-照片打印",
                "mch_id" to wxMchId,
                "nonce_str" to nonceStr,
                "notify_url" to notifyUrl,
                "openid" to openId,
                "out_trade_no" to order.orderNo,
                "spbill_create_ip" to ipAddress,
                "total_fee" to totalFee.toString(),
                "trade_type" to "JSAPI"
        )))

        val headers = HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");

        val request = HttpEntity<String>(requestBody, headers)
        val response = restTemplate.exchange(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                HttpMethod.POST,
                request,
                Resource::class.java)


        InputStreamReader(response.body.inputStream, StandardCharsets.UTF_8).use {
            val resultStr = String(response.body.inputStream.readBytes(), StandardCharsets.UTF_8)
            println(resultStr)

            val result = wxUnifyOrderResultUnmarshaller.unmarshal(StringReader(resultStr)) as WxUnifyOrderResult

            if (result.return_code == "SUCCESS" && result.result_code == "SUCCESS") {
                return createWxPayParams(result, nonceStr)
            }
            else {
                throw ProcessException(3, "return_code=${result.return_code}, result_code=${result.result_code}")
            }
        }
    }

    private fun createWxPayParams(res: WxUnifyOrderResult, nonceStr: String): WxPayParams {
        val timeStamp = (System.currentTimeMillis() / 1000).toString()

        val kvs = "appId=$wxAppId" +
                "&nonceStr=$nonceStr" +
                "&package=prepay_id=${res.prepay_id}" +
                "&signType=MD5" +
                "&timeStamp=$timeStamp" +
                "&key=$wxPayKey"

        val md5Digist = MessageDigest.getInstance("MD5")
        val sign = bytesToHexString(md5Digist.digest(kvs.toByteArray(StandardCharsets.US_ASCII)))

        return WxPayParams(
                timeStamp = timeStamp,
                nonceStr = nonceStr,
                pkg = "prepay_id=${res.prepay_id}",
                paySign = sign
        )
    }

    @Transactional
    override fun processWxPayNotify(requestBodyStr: String): String? {
        //XXX 记录到文件中
        //XXX 签名检查
        val result = wxPayNotifyUnmarshaller.unmarshal(StringReader(requestBodyStr)) as WxPayNotify

        if (result.appid != wxAppId) {
            return "错误的AppId"
        }
        else if (result.mch_id != wxMchId) {
            return "错误的商户号"
        }
        else if (result.out_trade_no == null) {
            return "订单号为空"
        }
        else {
            val printOrder = printOrderDao.findByOrderNo(result.out_trade_no!!)
            if (printOrder == null) {
                return "没有找到订单"
            }
            else {
                //XXX 检查金额

                printOrder.state = PrintOrderState.PAYED.value
                printOrderDao.save(printOrder)

                return null
            }
        }
    }

    private fun getPaymentRequestParams(varMap: TreeMap<String, String>): String {
        val sb = StringBuilder("<xml>")
        val sj = StringJoiner("&")

        for ((key, value) in varMap) {
            if (!value.isNullOrBlank()) {
                sj.add("$key=$value")
            }

            sb.append("<$key>$value</$key>")
        }

        sj.add("key=$wxPayKey")

        val md5Digist = MessageDigest.getInstance("MD5")
        val sign = bytesToHexString(md5Digist.digest(sj.toString().toByteArray(StandardCharsets.UTF_8)))

        sb.append("<sign>$sign</sign>")
        sb.append("</xml>")

        return sb.toString()
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val buf = StringBuilder()
        for (b in bytes) {
            buf.append(String.format("%02X", b and 0xff.toByte()))
        }
        return buf.toString()
    }
}



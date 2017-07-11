package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.WxPayParams
import com.unicolour.joyspace.dto.WxUnifyOrderResult
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.PrintOrderItem
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.PrintOrderService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
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
import javax.annotation.PostConstruct
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller


@Service
open class PrintOrderServiceImpl : PrintOrderService {
    @Autowired
    lateinit var printStationDao: PrintStationDao

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

    @PostConstruct
    fun initialize() {
        val jaxbContext = JAXBContext.newInstance(WxUnifyOrderResult::class.java)
        wxUnifyOrderResultUnmarshaller = jaxbContext.createUnmarshaller()
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
            newOrderItem.userImageFile = userImageFileDao.findOne(orderItem.imageFileId)

            printOrderItemDao.save(newOrderItem)
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
            arrayOf(printOrderItem.userImageFile)
        }
    }

    override fun getPrintOrderDataFetcher(): DataFetcher<PrintOrder> {
        return DataFetcher { env ->
            val printStationId = env.getArgument<Int>("printStationId")
            val idAfter = env.getArgument<Int>("idAfter")

            printOrderDao.findFirstByPrintStationIdAndIdAfter(printStationId, idAfter)
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

    override fun startPayment(orderId: Int): WxPayParams {
        val order = printOrderDao.findOne(orderId)
        val user = userDao.findOne(order.userId)
        val openId: String = user?.wxOpenId ?: ""

        var nonceStr: String = BigInteger(32 * 8, secureRandom).toString(36).toUpperCase()
        if (nonceStr.length > 32) {
            nonceStr = nonceStr.substring(0, 32)
        }
        val notifyUrl: String = "https://joyspace.uni-colour.com/wxpay/notify"
        val ipAddress: String = java.net.InetAddress.getByName("joyspace.uni-colour.com").hostAddress

        val totalFee:String = "1"   //XXX
        val requestBody = getPaymentRequestParams(TreeMap(hashMapOf<String, String>(
                "appid" to wxAppId,
                "attach" to "支付测试",
                "body" to "优利绚彩-照片打印",
                "mch_id" to wxMchId,
                "nonce_str" to nonceStr,
                "notify_url" to notifyUrl,
                "openid" to openId,
                "out_trade_no" to order.orderNo,
                "spbill_create_ip" to ipAddress,
                "total_fee" to totalFee,
                "trade_type" to "JSAPI"
        )))

        val headers = HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        val request = HttpEntity<String>(requestBody, headers)
        val response = restTemplate.exchange(
                "https://api.mch.weixin.qq.com/pay/unifiedorder",
                HttpMethod.POST,
                request,
                Resource::class.java)


        InputStreamReader(response.body.inputStream, StandardCharsets.UTF_8).use {
            val result = wxUnifyOrderResultUnmarshaller.unmarshal(InputStreamReader(response.body.inputStream, StandardCharsets.UTF_8)) as WxUnifyOrderResult

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
        val sign = bytesToHexString(md5Digist.digest(sj.toString().toByteArray(StandardCharsets.US_ASCII)))

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



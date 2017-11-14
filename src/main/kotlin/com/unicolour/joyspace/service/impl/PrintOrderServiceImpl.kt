package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ImageService
import com.unicolour.joyspace.service.PrintOrderService
import com.unicolour.joyspace.service.PrintStationService
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
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
import kotlin.collections.ArrayList


@Service
open class PrintOrderServiceImpl : PrintOrderService {
    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

    @Autowired
    lateinit var printStationService: PrintStationService

    @Autowired
    lateinit var imageService: ImageService

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
    lateinit var printOrderImageDao: PrintOrderImageDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var templateImageInfoDao: TemplateImageInfoDao

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

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

        val ret = calculateOrderFee(orderInput)

        val newOrder = PrintOrder()
        newOrder.orderNo = createOrderNo()
        newOrder.companyId = printStation.companyId
        newOrder.createTime = Calendar.getInstance()
        newOrder.updateTime = newOrder.createTime
        newOrder.printStationId = orderInput.printStationId
        newOrder.userId = session.userId
        newOrder.totalFee = ret.first
        newOrder.discount = ret.second
        //XXX newOrder.coupon = orderInput.coupon
        newOrder.payed = false
        newOrder.imageFileUploaded = false
        newOrder.downloadedToPrintStation = false
        newOrder.printedOnPrintStation = false

        val orderItems = ArrayList<PrintOrderItem>()
        newOrder.printOrderItems = orderItems

        printOrderDao.save(newOrder)

        for (orderItemInput in orderInput.orderItems) {
            //XXX 检查自助机是否支持此产品
            val product = productDao.findOne(orderItemInput.productId)

            val newOrderItem = PrintOrderItem()
            newOrderItem.copies = orderItemInput.copies
            newOrderItem.printOrder = newOrder
            newOrderItem.productId = orderItemInput.productId
            newOrderItem.productType = product.template.type
            newOrderItem.productVersion = orderItemInput.productVersion

            val orderImages = ArrayList<PrintOrderImage>()
            newOrderItem.orderImages = orderImages

            printOrderItemDao.save(newOrderItem)

            orderItems.add(newOrderItem)

            val tplVerSplit = orderItemInput.productVersion.split('.')
            val tplId = tplVerSplit[0].toInt()
            val tplVer = tplVerSplit[1].toInt()

            val tplImages = templateImageInfoDao.findByTemplateIdAndTemplateVersion(tplId, tplVer)
            for (tplImg in tplImages.filter { it.userImage }.distinctBy { it.name }) {
                var userImgId = 0
                var processParams: String? = null
                val orderItemImages = orderItemInput.images
                if (orderItemImages != null) {
                    val orderItemImg = orderItemImages.find { it.name == tplImg.name }
                    if (orderItemImg != null) {
                        userImgId = orderItemImg.imageId

                        val userImgFile = userImageFileDao.findOne(userImgId)
                        if (userImgFile != null) {
                            if (userImgFile.userId != session.userId) {
                                throw ProcessException(3, "图片不属于指定用户")
                            }
                        }

                        processParams = objectMapper.writeValueAsString(ImageProcessParams(orderItemImg))
                    }
                }

                val orderImg = PrintOrderImage()
                orderImg.orderId = newOrder.id
                orderImg.orderItemId = newOrderItem.id
                orderImg.name = tplImg.name
                orderImg.userImageFile = if (userImgId == 0) null else userImageFileDao.findOne(userImgId)
                orderImg.processParams = processParams

                printOrderImageDao.save(orderImg)

                orderImages.add(orderImg)
            }
        }

        return newOrder
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

    override val imageFilesDataFetcher: DataFetcher<Array<UserImageFile>>
        get() {
            return DataFetcher { env ->
                val printOrderItem = env.getSource<PrintOrderItem>()
                //printOrderItem.userImageFiles.toTypedArray()
                //XXX
                emptyArray<UserImageFile>()
            }
        }

    @Transactional
    override fun uploadOrderItemImage(sessionId: String, orderItemId: Int, name:String, imageProcessParam: ImageProcessParams, imgFile: MultipartFile?): Boolean {
        val imgInfo = imageService.uploadImage(sessionId, imgFile, "")
        if (imgInfo.errcode == 0) {
            val orderImg = printOrderImageDao.findByOrderItemIdAndName(orderItemId, name)
            if (orderImg == null) {
                throw ProcessException(2, "没有此名称的图片")
            }
            else {
                orderImg.userImageFile = userImageFileDao.findOne(imgInfo.imageId)
                orderImg.processParams = objectMapper.writeValueAsString(imageProcessParam)

                printOrderImageDao.save(orderImg)
                return checkOrderImageUploaded(orderImg.orderId)
            }
        }
        else {
            throw ProcessException(1, "上传图片失败")
            //XXX
        }
    }

    //检查是否所有订单图片都已上传，如果都上传了返回true并修改订单状态
    @Synchronized
    private fun checkOrderImageUploaded(orderId: Int) : Boolean {
        val missingImageCount = printOrderImageDao.countByOrderIdAndUserImageFileIdIsNull(orderId)  //userImageFileId is null 表示此订单图片还没有上传
        if (missingImageCount == 0L) {
            val order= printOrderDao.findOne(orderId)
            order.imageFileUploaded = true
            printOrderDao.save(order)
            return true
        }
        else {
            return false
        }
    }

    override val printOrderDataFetcher: DataFetcher<PrintOrder?>
        get() {
            return DataFetcher { env ->
                val sessionId = env.getArgument<String>("sessionId")
                val idAfter = env.getArgument<Int>("idAfter")

                val session = printStationLoginSessionDao.findOne(sessionId)
                if (session == null) {
                    null
                }
                else {
                    printOrderDao.findFirstByPrintStationIdAndPayedAndImageFileUploadedAndIdAfter(
                            printStationId = session.printStationId,
                            payed = true,
                            imageFileUploaded = true,
                            idAfter = idAfter)
                }
            }
        }

    override val printerOrderDownloadedDataFetcher: DataFetcher<GraphQLRequestResult>
        get() {
            return DataFetcher { env ->
                updatePrintOrderState(env, "downloaded")
            }
        }

    override val printerOrderPrintedDataFetcher: DataFetcher<GraphQLRequestResult>
        get() {
            return DataFetcher { env ->
                updatePrintOrderState(env, "printed")
            }
        }

    private fun updatePrintOrderState(env: DataFetchingEnvironment, state: String): GraphQLRequestResult {
        val sessionId = env.getArgument<String>("sessionId")
        val printOrderId = env.getArgument<Int>("printOrderId")

        val loginSession = printStationLoginSessionDao.findOne(sessionId)
        return if (loginSession == null || System.currentTimeMillis() > loginSession.expireTime.timeInMillis) {
            GraphQLRequestResult(ResultCode.INVALID_PRINT_STATION_LOGIN_SESSION)
        } else {
            val order = printOrderDao.findOne(printOrderId)
            if (order != null) {
                if (order.printStationId != loginSession.printStationId) {
                    GraphQLRequestResult(ResultCode.NOT_IN_THIS_PRINT_STATION)
                } else {
                    if (state == "downloaded") {
                        order.downloadedToPrintStation = true
                    }
                    else if (state == "printed") {
                        order.printedOnPrintStation = true
                    }

                    printOrderDao.save(order)
                    GraphQLRequestResult(ResultCode.SUCCESS)
                }
            } else {
                GraphQLRequestResult(ResultCode.PRINT_ORDER_NOT_FOUND)
            }
        }
    }

    override fun calculateOrderFee(orderInput: OrderInput) : Pair<Int, Int> {
        val printStation = printStationDao.findOne(orderInput.printStationId)
        val priceMap = printStationService.getPriceMap(printStation)

        val productIdObjMap: MutableMap<Int, Product> = HashMap()
        var totalFee = 0
        var discount = 0

        for (printOrderItem in orderInput.orderItems) {
            val orderItemFee:Int = priceMap.getOrElse(printOrderItem.productId, {
                val product = productIdObjMap.computeIfAbsent(printOrderItem.productId, { productId -> productDao.findOne(productId) })
                product.defaultPrice
            })
            totalFee += orderItemFee * printOrderItem.copies
        }

        //XXX
        if (!orderInput.coupon.isNullOrEmpty()) {
            if (orderInput.coupon == "J5") {
                if (totalFee > 10) {
                    discount = 5
                }
            }
            else if (orderInput.coupon == "8Z") {
                discount = (totalFee * 0.8).toInt()
            }
            else {
                throw ProcessException(1, "指定的优惠券不可用")
            }
        }

        return Pair(totalFee, discount)
    }

    override fun startPayment(orderId: Int, baseUrl:String): WxPayParams {
        val order = printOrderDao.findOne(orderId)

        val user = userDao.findOne(order.userId)
        val openId: String = user?.wxOpenId ?: ""

        var nonceStr: String = BigInteger(32 * 8, secureRandom).toString(36).toUpperCase()
        if (nonceStr.length > 32) {
            nonceStr = nonceStr.substring(0, 32)
        }
        val notifyUrl = "$baseUrl/wxpay/notify"
        val ipAddress: String = java.net.InetAddress.getByName(URL(baseUrl).host).hostAddress

        val requestBody = getPaymentRequestParams(TreeMap(hashMapOf<String, String>(
                "appid" to wxAppId,
                "body" to "优利绚彩-照片打印",
                "mch_id" to wxMchId,
                "nonce_str" to nonceStr,
                "notify_url" to notifyUrl,
                "openid" to openId,
                "out_trade_no" to order.orderNo,
                "spbill_create_ip" to ipAddress,
                "total_fee" to (order.totalFee - order.discount).toString(),
                "trade_type" to "JSAPI"
        )))

        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8")

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

                printOrder.payed = true
                printOrderDao.save(printOrder)

                return null
            }
        }
    }

    private fun getPaymentRequestParams(varMap: TreeMap<String, String>): String {
        val sb = StringBuilder("<xml>")
        val sj = StringJoiner("&")

        for ((key, value) in varMap) {
            if (!value.isBlank()) {
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



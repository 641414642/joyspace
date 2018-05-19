package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.ProductImageFileType
import com.unicolour.joyspace.service.PrintOrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.servlet.http.HttpServletRequest


@RestController
class ApiOrderRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var printOrderService: PrintOrderService
    @Autowired
    private lateinit var printOrderDao: PrintOrderDao
    @Autowired
    private lateinit var userLoginSessionDao: UserLoginSessionDao
    @Autowired
    private lateinit var userDao: UserDao
    @Autowired
    private lateinit var productDao: ProductDao
    @Autowired
    private lateinit var printStationDao: PrintStationDao
    @Autowired
    private lateinit var couponDao: CouponDao

    /**
     * 创建订单
     */
    @PostMapping(value = "/v2/order/create")
    fun createOrder(@RequestBody orderInput: OrderInput): RestResponse {
        try {
            val order = printOrderService.createOrder(orderInput)
            if ((order.totalFee - order.discount) <= 0) {
                order.payed = true
                order.updateTime = Calendar.getInstance()
                printOrderDao.save(order)
            }
            val params = printOrderService.startPayment(order.id)
            //val params: WxPayParams? = null
            val orderItems = order.printOrderItems.map { OrderItemRet(it.id, it.productId) }
            return RestResponse.ok(CreateOrderRequestResult(order.id, order.orderNo, params, orderItems, order.totalFee, order.discount))
        } catch (e: ProcessException) {
            e.printStackTrace()
            return RestResponse(e.errcode, null, e.message)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return RestResponse(1, null, ex.message)
        }
    }

    /**
     * 支付订单
     */
    @PostMapping(value = "/v2/order/pay")
    fun payOrder(@RequestBody orderInput: OrderPayInput): RestResponse {
        try {
            val session = userLoginSessionDao.findOne(orderInput.sessionId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
            userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
            val order = printOrderDao.findOne(orderInput.orderId) ?: return RestResponse.error(ResultCode.PRINT_ORDER_NOT_FOUND)
            if ((order.totalFee - order.discount) <= 0){
                order.payed = true
                order.updateTime = Calendar.getInstance()
                printOrderDao.save(order)
            }
            val params = printOrderService.startPayment(orderInput.orderId)
            val orderItems = order.printOrderItems.map { OrderItemRet(it.id, it.productId) }
            return RestResponse.ok(CreateOrderRequestResult(order.id, order.orderNo, params, orderItems, order.totalFee, order.discount))
        } catch (e: ProcessException) {
            e.printStackTrace()
            return RestResponse(e.errcode, null, e.message)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return RestResponse(1, null, ex.message)
        }
    }


    /**
     * 取消订单
     */
    @PostMapping(value = "/v2/order/cancel")
    fun cancelOrder(@RequestBody param: OrderCancelInput): RestResponse {
        val session = userLoginSessionDao.findOne(param.sessionId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val order = printOrderDao.findOne(param.orderId)
        order.canceled = true
        order.updateTime = Calendar.getInstance()
        printOrderDao.save(order)
        return RestResponse.ok()
    }

    //微信支付回调


    //查看订单图片状态
    @GetMapping("/v2/order/status")
    fun printOrderStatus(@RequestParam("sessionId") sessionId: String,
                         @RequestParam("printOrderId") printOrderId: Int): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
        userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val printOrder = printOrderDao.findOne(printOrderId)
        if (printOrder != null && printOrder.userId == session.userId) {
            val orderItemVoList = printOrder.printOrderItems.map {
                OrderItemS(listOf(ImageS(it.status)))
            }
            return RestResponse.ok(OrderStatusVo(orderItemVoList))
        } else {
            return RestResponse(1, null, "不是此用户的订单")
        }
    }


    //上传订单图片
    @PostMapping("/v2/order/image")
    fun uploadOrderItemImage(request: HttpServletRequest,
                             @RequestParam("sessionId") sessionId: String,
                             @RequestParam("orderItemId") orderItemId: Int,
                             @RequestParam("name") name: String,
                             @RequestParam("image") imgFile: MultipartFile?): ResponseEntity<UploadOrderImageResult> {

        val allUploaded = printOrderService.uploadOrderImage(sessionId, orderItemId, imgFile)
        return ResponseEntity.ok(UploadOrderImageResult(allUploaded))
    }


    /**
     * 获取用户全部订单
     */
    @GetMapping(value = "/v2/order/list")
    fun listOrder(@RequestParam("sessionId") sessionId: String): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
        val user = userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val orderList = printOrderDao.findByUserId(user.id)
        val orderListVo = orderList.filter { !it.canceled }.map {
            var status = 0
            if (!it.payed) status = 0
            if (it.payed && !it.printedOnPrintStation) status = 1
            if (it.printedOnPrintStation) status = 2
            val product = productDao.findOne(it.printOrderItems.first().productId)
            val thumbnailImageUrl = product.imageFiles
                    .filter { it.type == ProductImageFileType.THUMB.value }
                    .map { "/assets/product/images/${it.id}.${it.fileType}" }
                    .firstOrNull()
            val productType = it.printOrderItems.first().productType
            val productTypeStr = com.unicolour.joyspace.model.ProductType.values().first { it.value == productType }.dispName
            OrderSimpleVo(it.id,
                    it.orderNo,
                    it.totalFee,
                    it.discount,
                    0,
                    it.createTime,
                    status,
                    it.updateTime,
                    product.name,
                    it.pageCount,
                    productType,
                    productTypeStr,
                    thumbnailImageUrl,
                    0)
        }
        return RestResponse.ok(orderListVo)
    }

    /**
     * 获取订单详情
     */
    @GetMapping(value = "/v2/order")
    fun getOrder(@RequestParam("sessionId") sessionId: String,
                 @RequestParam("orderId") orderId: Int): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val order = printOrderDao.findOne(orderId) ?: return RestResponse.error(ResultCode.PRINT_ORDER_NOT_FOUND)
        var status = 0
        if (!order.payed) status = 0
        if (order.payed && !order.printedOnPrintStation) status = 1
        if (order.printedOnPrintStation) status = 2
        val product = productDao.findOne(order.printOrderItems.first().productId)
        val thumbnailImageUrl = product.imageFiles
                .filter { it.type == ProductImageFileType.THUMB.value }
                .map { "/assets/product/images/${it.id}.${it.fileType}" }
                .firstOrNull()
        val productType = order.printOrderItems.first().productType
        val productTypeStr = com.unicolour.joyspace.model.ProductType.values().first { it.value == productType }.dispName
        val printStation = printStationDao.findOne(order.printStationId)
        var couponVo:CouponVo? = null
        if (order.couponId != null) {
            val coupon = couponDao.findOne(order.couponId)
            couponVo = CouponVo(coupon.id,
                    coupon.name,
                    coupon.code,
                    coupon.begin,
                    coupon.expire,
                    coupon.minExpense,
                    coupon.discount,
                    1)
        }

        val psVo = PrintStationVo()
        psVo.id = printStation.id
        psVo.address = printStation.addressNation + printStation.addressProvince + printStation.addressCity + printStation.addressDistrict + printStation.addressStreet
        psVo.longitude = printStation.position.longitude
        psVo.latitude = printStation.position.latitude
        psVo.wxQrCode = printStation.wxQrCode
        psVo.positionId = printStation.positionId.toString()
        psVo.companyId = printStation.companyId.toString()
        psVo.status = printStation.status
        psVo.name = printStation.name
        psVo.imgUrl = ""
        val resOrderVo = OrderSimpleVo(order.id,
                order.orderNo,
                order.totalFee,
                order.discount,
                0,
                order.createTime,
                status,
                order.updateTime,
                product.name,
                order.pageCount,
                productType,
                productTypeStr,
                thumbnailImageUrl,
                0,
                psVo,
                couponVo)

        return RestResponse.ok(resOrderVo)
    }


}
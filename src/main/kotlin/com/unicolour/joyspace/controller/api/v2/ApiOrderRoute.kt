package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.PrintOrderDao
import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.service.PrintOrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest


@RestController
class ApiOrderRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var printOrderService: PrintOrderService
    @Autowired
    private lateinit var printOrderDao: PrintOrderDao
    @Autowired
    private lateinit var userLoginSessionDao:UserLoginSessionDao
    @Autowired
    private lateinit var userDao:UserDao

    /**
     * 创建订单
     */
    @PostMapping(value = "/v2/order/create")
    fun createOrder(@RequestBody orderInput: OrderInput): RestResponse {
        try {
            val order = printOrderService.createOrder(orderInput)
            val params = printOrderService.startPayment(order.id)
            //val params: WxPayParams? = null
            val orderItems = order.printOrderItems.map { OrderItemRet(it.id, it.productId) }
            return RestResponse.ok(CreateOrderRequestResult(order.id, order.orderNo, params, orderItems, order.totalFee, order.discount))
        } catch(e: ProcessException) {
            e.printStackTrace()
            return RestResponse(e.errcode,null,e.message)
        }  catch (ex: Exception) {
            ex.printStackTrace()
            return RestResponse(1,null,ex.message)
        }
    }


    /**
     * 取消订单
     */
    @PostMapping(value = "/v2/order/cancel")
    fun cancelOrder(): RestResponse {
        val order = OrderVo()
        return RestResponse.ok(order)
    }

    //微信支付回调


    //查看订单图片状态





    //上传订单图片
    @RequestMapping("/api/order/image", method = arrayOf(RequestMethod.POST))
    fun uploadOrderItemImage(request: HttpServletRequest,
                             @RequestParam("sessionId") sessionId: String,
                             @RequestParam("orderItemId") orderItemId: Int,
                             @RequestParam("name") name: String,
                             @RequestParam("image") imgFile: MultipartFile?) : ResponseEntity<UploadOrderImageResult> {

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
        return RestResponse.ok(orderList)
    }


}
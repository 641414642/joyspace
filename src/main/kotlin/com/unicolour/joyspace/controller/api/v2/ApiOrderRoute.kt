package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.PrintOrderDao
import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.OrderVo
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.service.PrintOrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


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
    fun createOrder(): RestResponse {
        val order = OrderVo()
        return RestResponse.ok(order)
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
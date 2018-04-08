package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dto.OrderVo
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.service.PrintOrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiOrderRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var printOrderService: PrintOrderService

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


}
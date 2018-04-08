package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dto.CouponVo
import com.unicolour.joyspace.dto.common.RestResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiCouponRoute {
    val logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * 返回用户优惠券列表
     */
    @GetMapping(value = "/v2/coupons")
    fun findUserCoupons(): RestResponse {
        val coupons = mutableListOf<CouponVo>()
        return RestResponse.ok(coupons)
    }


    /**
     * 返回用户该比订单可用优惠券列表
     */
    @GetMapping(value = "/v2/coupons/order")
    fun getCouponsByOrder(): RestResponse {
        val coupons = mutableListOf<CouponVo>()
        return RestResponse.ok(coupons)
    }


    /**
     * 用户领取优惠券
     */
    @PostMapping(value = "/v2/user/claimCoupon")
    fun claimCoupon(): RestResponse {
        val coupon = CouponVo()
        return RestResponse.ok(coupon)
    }

}
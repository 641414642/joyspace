package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.Coupon

//用户优惠券列表的结果
class UserCouponListResult (
        //结果代码 0: 成功
        var result: Int = 0,

        //结果的描述
        var description: String? = null,

        //优惠券列表
        var coupons: List<Coupon> = emptyList()
)

//用户获取优惠券请求的结果
class ClaimCouponResult (
        //结果代码 0: 成功
        var result: Int = 0,

        //结果的描述
        var description: String? = null,

        //优惠券信息
        var coupon: Coupon? = null
)
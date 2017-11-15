package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.UserCoupon
import org.springframework.data.repository.CrudRepository

interface UserCouponDao : CrudRepository<UserCoupon, Int> {
    fun findByUserId(userId: Int): List<UserCoupon>
    fun findByUserIdAndCouponId(userId: Int, couponId: Int) : UserCoupon?
}
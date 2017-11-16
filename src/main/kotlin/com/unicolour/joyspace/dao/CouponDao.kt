package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Coupon
import org.springframework.data.repository.CrudRepository

interface CouponDao : CrudRepository<Coupon, Int> {
    fun findByIdIn(idList: List<Int>): List<Coupon>
    fun findByCode(code: String): Coupon
}

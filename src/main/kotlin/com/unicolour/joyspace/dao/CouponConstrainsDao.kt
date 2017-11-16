package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.CouponConstrains
import org.springframework.data.repository.CrudRepository

interface CouponConstrainsDao: CrudRepository<CouponConstrains, Int> {
    fun findByCouponId(id: Int): List<CouponConstrains>
}
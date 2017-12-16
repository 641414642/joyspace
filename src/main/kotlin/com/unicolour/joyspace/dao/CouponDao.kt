package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Coupon
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface CouponDao : PagingAndSortingRepository<Coupon, Int> {
    fun findByIdInOrderByDiscountDesc(idList: List<Int>): List<Coupon>
    fun findByCodeIgnoreCase(code: String): Coupon?

    fun findByIdNotIn(couponIds: List<Int>): Iterable<Coupon>

    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<Coupon>
    fun findByNameIgnoreCaseAndCompanyId(name: String, companyId: Int, pageable: Pageable): Page<Coupon>
}

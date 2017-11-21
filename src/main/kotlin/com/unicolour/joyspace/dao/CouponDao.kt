package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Coupon
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface CouponDao : PagingAndSortingRepository<Coupon, Int> {
    fun findByIdInOrderByDiscountDesc(idList: List<Int>): List<Coupon>
    fun findByCode(code: String): Coupon

    @Query("SELECT t FROM Coupon t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable): Page<Coupon>

    fun findByIdNotIn(couponIds: List<Int>): Iterable<Coupon>
}

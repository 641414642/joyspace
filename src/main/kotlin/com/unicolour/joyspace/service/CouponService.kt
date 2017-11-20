package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.ClaimCouponResult
import com.unicolour.joyspace.dto.UserCouponListResult
import com.unicolour.joyspace.model.*
import graphql.schema.DataFetcher
import java.util.*

enum class CouponValidateResult(val desc:String) {
    VALID("有效"),

    USER_NOT_CLAIM_THIS_COUPON("用户未领取此优惠券"),

    NOT_BEGIN("还未进入有效期"),
    EXPIRED("已经超出有效期"),

    EXCEED_MAX_USAGE("超过了最大使用次数"),
    EXCEED_MAX_USAGE_PER_USER("超过了每用户最大使用次数")
}

class CouponValidateContext(
        val coupon: Coupon,
        val constrains: List<CouponConstrains>,
        val userCoupon: UserCoupon? = null,
        val userId: Int = 0,
        val printStationId: Int = 0,
        val positionId: Int = 0,
        val companyId: Int = 0
)

interface CouponService {
    val userCouponListDataFetcher: DataFetcher<UserCouponListResult>
    val claimCouponDataFetcher: DataFetcher<ClaimCouponResult>

    /**
     * 检查优惠券是否在有效期内
     * @param context
     * @return
     */
    fun validateCouponByTime(context: CouponValidateContext): CouponValidateResult

    /**
     * 检查优惠券是否超出最大使用次数
     * @param context
     * @return
     */
    fun validateCouponByMaxUses(context: CouponValidateContext): CouponValidateResult

    /**
     * 检查优惠券是否超出用户最大使用次数
     * @param context
     * @return
     */
    fun validateCouponByMaxUsesPerUser(context: CouponValidateContext): CouponValidateResult

    fun validateCoupon(context: CouponValidateContext,
                       vararg validateFuns: (CouponValidateContext) -> CouponValidateResult): CouponValidateResult

    fun getDataFetcher(fieldName:String): DataFetcher<Any>

    fun createCoupon(name: String, code: String, couponGetMethod: CouponGetMethod,
                     maxUses: Int, maxUsesPerUser: Int, minExpense: Int, discount: Int,
                     begin: Date, expire: Date,
                     selectedProductTypes: Set<ProductType>,
                     selectedProductIds: Set<Int>,
                     selectedPositionIds: Set<Int>,
                     selectedPrintStationIds: Set<Int>)

    fun updateCoupon(id: Int, name: String, code: String, couponGetMethod: CouponGetMethod,
                     maxUses: Int, maxUsesPerUser: Int, minExpense: Int, discount: Int,
                     begin: Date, expire: Date,
                     selectedProductTypes: Set<ProductType>,
                     selectedProductIds: Set<Int>,
                     selectedPositionIds: Set<Int>,
                     selectedPrintStationIds: Set<Int>): Boolean
}
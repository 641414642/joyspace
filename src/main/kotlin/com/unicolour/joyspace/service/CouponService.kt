package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.ClaimCouponResult
import com.unicolour.joyspace.dto.UserCouponListResult
import com.unicolour.joyspace.model.*
import graphql.schema.DataFetcher
import java.util.*

enum class CouponValidateResult(val desc:String) {
    VALID("有效"),

    COUPON_NOT_EXIST("没有此优惠券"),
    USER_NOT_CLAIM_THIS_COUPON("用户未领取此优惠券"),

    NOT_BEGIN("还未进入有效期"),
    EXPIRED("已经超出有效期"),

    EXCEED_MAX_USAGE("超过了最大使用次数"),
    EXCEED_MAX_USAGE_PER_USER("超过了每用户最大使用次数"),

    USER_REG_NOT_LONG_ENOUGH("用户注册时间不够"),

    NOT_USABLE_IN_THIS_PRINT_STATION("不能在此自助机使用"),

    DISABLED("已禁用")
}

class CouponValidateContext(
        val coupon: Coupon,
        val userCoupon: UserCoupon? = null,
        val userId: Int = 0,
        val user: User? = null,
        val printStationId: Int = 0,
        val positionId: Int = 0,
        val companyId: Int = 0,
        val claimMethod: CouponClaimMethod = CouponClaimMethod.SCAN_PRINT_STATION_CODE
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
     * 检查优惠券是否已禁用
     * @param context
     * @return
     */
    fun validateCouponEnabled(context: CouponValidateContext): CouponValidateResult

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

    /**
     * 检查优惠券领取方式
     * @param context
     * @return
     */
    fun validateCouponByClaimMethod(context: CouponValidateContext): CouponValidateResult

    /**
     * 检查用户注册时间
     * @param context
     * @return
     */
    fun validateCouponByUserRegTime(context: CouponValidateContext): CouponValidateResult

    /**
     * 检查自助机
     * @param context
     * @return
     */
    fun validateCouponByPrintStation(context: CouponValidateContext): CouponValidateResult

    fun validateCoupon(context: CouponValidateContext,
                       vararg validateFuns: (CouponValidateContext) -> CouponValidateResult): CouponValidateResult

    fun getDataFetcher(fieldName:String): DataFetcher<Any>

    fun createCoupon(name: String, code: String, enabled: Boolean, couponClaimMethod: CouponClaimMethod,
                     maxUses: Int, maxUsesPerUser: Int, minExpense: Int, discount: Int,
                     begin: Date, expire: Date, userRegDays: Int,
                     selectedProductTypes: Set<ProductType>,
                     selectedProductIds: Set<Int>,
                     selectedPositionIds: Set<Int>,
                     selectedPrintStationIds: Set<Int>)

    fun updateCoupon(id: Int, name: String, code: String, enabled: Boolean, couponClaimMethod: CouponClaimMethod,
                     maxUses: Int, maxUsesPerUser: Int, minExpense: Int, discount: Int,
                     begin: Date, expire: Date, userRegDays: Int,
                     selectedProductTypes: Set<ProductType>,
                     selectedProductIds: Set<Int>,
                     selectedPositionIds: Set<Int>,
                     selectedPrintStationIds: Set<Int>): Boolean

    /**
     * @see userCouponListDataFetcher
     */
    fun summaryUserCouponId(session: UserLoginSession, printStationId: Int, user: User?, invalid: ArrayList<Int>): ArrayList<Int>

    /**
     * @see claimCouponDataFetcher
     */
    fun claimCouponResult(code: String, session: UserLoginSession, user: User?): ClaimCouponResult

    fun summaryCouponIdByOrder(session: UserLoginSession, printStationId: Int, user: User?,fee:Int, invalid: ArrayList<Int>): ArrayList<Int>
}
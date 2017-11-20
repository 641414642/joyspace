package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.CouponConstrainsDao
import com.unicolour.joyspace.dao.CouponDao
import com.unicolour.joyspace.dao.UserCouponDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.ClaimCouponResult
import com.unicolour.joyspace.dto.UserCouponListResult
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.CouponService
import com.unicolour.joyspace.service.CouponValidateContext
import com.unicolour.joyspace.service.CouponValidateResult
import com.unicolour.joyspace.service.CouponValidateResult.*
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.transaction.Transactional

@Component
open class CouponServiceImpl : CouponService {
    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var couponDao: CouponDao

    @Autowired
    lateinit var couponConstrainsDao: CouponConstrainsDao

    @Autowired
    lateinit var userCouponDao: UserCouponDao

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    private var couponLock = Object()

    override val userCouponListDataFetcher: DataFetcher<UserCouponListResult>
        get() {
            return DataFetcher { env ->
                val sessionId = env.getArgument<String>("sessionId")
                val session = userLoginSessionDao.findOne(sessionId)

                if (session == null) {  //XXX 登录过期检查
                    UserCouponListResult(1, "用户未登录")
                }
                else {
                    val userCoupons = userCouponDao.findByUserId(session.userId)
                    UserCouponListResult(0, null,
                            couponDao.findByIdIn(userCoupons.map { it.couponId }))
                }
            }
        }

    override val claimCouponDataFetcher: DataFetcher<ClaimCouponResult>
        get() {
            return DataFetcher { env ->
                val sessionId = env.getArgument<String>("sessionId")
                val session = userLoginSessionDao.findOne(sessionId)
                val code = env.getArgument<String>("couponCode")

                if (session == null) {  //XXX 登录过期检查
                    ClaimCouponResult(1, "用户未登录")
                }
                else {
                    synchronized(couponLock, {
                        transactionTemplate.execute {
                            val coupon = couponDao.findByCode(code)
                            val constrains = couponConstrainsDao.findByCouponId(coupon.id)

                            val oldUserCoupon = userCouponDao.findByUserIdAndCouponId(session.userId, coupon.id)
                            if (oldUserCoupon != null) {
                                ClaimCouponResult(1, "用户已经领取过此优惠券")
                            }
                            else {
                                val context = CouponValidateContext(
                                        coupon = coupon,
                                        constrains = constrains)

                                //XXX 用户maxUse check
                                val checkResult = validateCoupon(context,
                                        this::validateCouponByTime,
                                        this::validateCouponByMaxUses)

                                if (checkResult == VALID) {
                                    val userCoupon = UserCoupon()
                                    userCoupon.couponId = coupon.id
                                    userCoupon.userId = session.userId
                                    userCoupon.usageCount = 0
                                    userCoupon.claimTime = Date()
                                    userCouponDao.save(userCoupon)

                                    coupon.claimCount++
                                    couponDao.save(coupon)

                                    ClaimCouponResult(0, null, coupon)
                                } else {
                                    ClaimCouponResult(1, checkResult.desc)
                                }
                            }
                        }
                    })
                }
            }
        }

    override fun getDataFetcher(fieldName: String): DataFetcher<Any> {
        return DataFetcher<Any> { env ->
            val coupon = env.getSource<Coupon>()
            when (fieldName) {
                "printStationIdList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRINT_STATION.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                "positionIdList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                "companyIdList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.COMPANY.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                "productIdList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                "productTypeList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                else -> null
            }
        }
    }

    override fun validateCoupon(context: CouponValidateContext,
                       vararg validateFuns: (CouponValidateContext) -> CouponValidateResult)
            : CouponValidateResult {
        for (validateFun in validateFuns) {
            val result = validateFun(context)
            if (result != VALID) {
                return result
            }
        }

        return VALID
    }

    override fun validateCouponByMaxUses(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon
        return if (coupon.usageCount < coupon.maxUses) {
            VALID
        }
        else {
            EXCEED_MAX_USAGE
        }
    }

    override fun validateCouponByMaxUsesPerUser(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon

        return if (context.userCoupon == null) {
            USER_NOT_CLAIM_THIS_COUPON
        }
        else if (coupon.maxUsesPerUser > 0 && context.userCoupon.usageCount >= coupon.maxUsesPerUser) {
            EXCEED_MAX_USAGE_PER_USER
        }
        else {
            VALID
        }
    }

    override fun validateCouponByTime(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon
        val now = System.currentTimeMillis()
        if (coupon.begin != null && now < coupon.begin!!.time) {
            return NOT_BEGIN
        }
        else if (coupon.expire != null && now > coupon.expire!!.time) {
            return EXPIRED
        }
        else {
            return VALID
        }
    }

    @Transactional
    override fun createCoupon(name: String, code: String, couponGetMethod: CouponGetMethod, maxUses: Int,
                              maxUsesPerUser: Int, minExpense: Int, discount: Int, begin: Date, expire: Date,
                              selectedProductTypes: Set<ProductType>,
                              selectedProductIds: Set<Int>,
                              selectedPositionIds: Set<Int>,
                              selectedPrintStationIds: Set<Int>) {
        val coupon = Coupon()
        coupon.name = name
        coupon.code = code
        coupon.getMethod = couponGetMethod.value
        coupon.maxUses = maxUses
        coupon.maxUsesPerUser = maxUsesPerUser
        coupon.minExpense = minExpense
        coupon.discount = discount
        coupon.begin = begin
        coupon.expire = expire

        couponDao.save(coupon)

        couponConstrainsDao.save(
                selectedProductTypes.map {
                    val couponProduct = CouponConstrains()
                    couponProduct.constrainsType = CouponConstrainsType.PRODUCT_TYPE.value
                    couponProduct.couponId = coupon.id
                    couponProduct.value = it.value
                    couponProduct
                })
        couponConstrainsDao.save(
                selectedProductIds.map {
                    val couponProduct = CouponConstrains()
                    couponProduct.constrainsType = CouponConstrainsType.PRODUCT.value
                    couponProduct.couponId = coupon.id
                    couponProduct.value = it
                    couponProduct
                })
        couponConstrainsDao.save(
                selectedPositionIds.map {
                    val couponProduct = CouponConstrains()
                    couponProduct.constrainsType = CouponConstrainsType.POSITION.value
                    couponProduct.couponId = coupon.id
                    couponProduct.value = it
                    couponProduct
                })
        couponConstrainsDao.save(
                selectedPrintStationIds.map {
                    val couponProduct = CouponConstrains()
                    couponProduct.constrainsType = CouponConstrainsType.PRINT_STATION.value
                    couponProduct.couponId = coupon.id
                    couponProduct.value = it
                    couponProduct
                })
    }

    @Transactional
    override fun updateCoupon(id: Int, name: String, code: String, couponGetMethod: CouponGetMethod, maxUses: Int,
                              maxUsesPerUser: Int, minExpense: Int, discount: Int, begin: Date, expire: Date,
                              selectedProductTypes: Set<ProductType>,
                              selectedProductIds: Set<Int>,
                              selectedPositionIds: Set<Int>,
                              selectedPrintStationIds: Set<Int>): Boolean {
        val coupon = couponDao.findOne(id)
        if (coupon == null) {
            return false
        }
        else {
            coupon.name = name
            coupon.code = code
            coupon.getMethod = couponGetMethod.value
            coupon.maxUses = maxUses
            coupon.maxUsesPerUser = maxUsesPerUser
            coupon.minExpense = minExpense
            coupon.discount = discount
            coupon.begin = begin
            coupon.expire = expire

            couponDao.save(coupon)

            couponConstrainsDao.deleteByCouponId(coupon.id)
            couponConstrainsDao.save(
                    selectedProductTypes.map {
                        val couponProduct = CouponConstrains()
                        couponProduct.constrainsType = CouponConstrainsType.PRODUCT_TYPE.value
                        couponProduct.couponId = coupon.id
                        couponProduct.value = it.value
                        couponProduct
                    })
            couponConstrainsDao.save(
                    selectedProductIds.map {
                        val couponProduct = CouponConstrains()
                        couponProduct.constrainsType = CouponConstrainsType.PRODUCT.value
                        couponProduct.couponId = coupon.id
                        couponProduct.value = it
                        couponProduct
                    })
            couponConstrainsDao.save(
                    selectedPositionIds.map {
                        val couponProduct = CouponConstrains()
                        couponProduct.constrainsType = CouponConstrainsType.POSITION.value
                        couponProduct.couponId = coupon.id
                        couponProduct.value = it
                        couponProduct
                    })
            couponConstrainsDao.save(
                    selectedPrintStationIds.map {
                        val couponProduct = CouponConstrains()
                        couponProduct.constrainsType = CouponConstrainsType.PRINT_STATION.value
                        couponProduct.couponId = coupon.id
                        couponProduct.value = it
                        couponProduct
                    })

            return true
        }
    }
}

package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.ClaimCouponResult
import com.unicolour.joyspace.dto.UserCouponListResult
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.CouponService
import com.unicolour.joyspace.service.CouponValidateContext
import com.unicolour.joyspace.service.CouponValidateResult
import com.unicolour.joyspace.service.CouponValidateResult.*
import com.unicolour.joyspace.service.ManagerService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Component
open class CouponServiceImpl : CouponService {


    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var couponDao: CouponDao

    @Autowired
    lateinit var couponConstrainsDao: CouponConstrainsDao

    @Autowired
    lateinit var userCouponDao: UserCouponDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    private var couponLock = Object()

    override val userCouponListDataFetcher: DataFetcher<UserCouponListResult>
        get() {
            return DataFetcher { env ->
                val sessionId = env.getArgument<String>("sessionId")
                val printStationId = env.getArgument<Int>("printStationId")
                val session = userLoginSessionDao.findOne(sessionId)

                if (session == null) {  //XXX 登录过期检查
                    UserCouponListResult(1, "用户未登录")
                }
                else {
                    val user = userDao.findOne(session.userId)

                    synchronized(couponLock, {
                        transactionTemplate.execute {
                            val retCouponIds = summaryUserCouponId(session, printStationId, user, ArrayList())

                            UserCouponListResult(0, null,
                                    couponDao.findByIdInOrderByDiscountDesc(retCouponIds))
                        }
                    })
                }
            }
        }

    override fun summaryCouponIdByOrder(session: UserLoginSession, printStationId: Int, user: User?, fee: Int, invalid: java.util.ArrayList<Int>): java.util.ArrayList<Int> {
        val userCoupons = userCouponDao.findByUserId(session.userId)
        val couponIds = userCoupons.map { it.couponId }
        val retCouponIds = ArrayList<Int>(couponIds)

        //删除已禁用、不存在、过期、超过最大使用次数的优惠券
        for (userCoupon in userCoupons) {
            val coupon = couponDao.findOne(userCoupon.couponId)
            val context = CouponValidateContext(
                    coupon = coupon,
                    userCoupon = userCoupon)

            val checkResult =
                    if (coupon == null) {
                        COUPON_NOT_EXIST
                    } else {
                        validateCoupon(context,
                                this::validateCouponEnabled,
                                this::validateCouponByTime,
                                this::validateCouponByMaxUses,
                                this::validateCouponByMaxUses)
                    }

            if (checkResult != VALID) {
                //userCouponDao.delete(userCoupon)
                invalid.add(userCoupon.couponId)
                retCouponIds.remove(userCoupon.couponId)
            }
            if (fee < coupon.minExpense) {
                invalid.add(userCoupon.couponId)
                retCouponIds.remove(userCoupon.couponId)
            }
        }
        return retCouponIds
    }

    override fun summaryUserCouponId(session: UserLoginSession, printStationId: Int, user: User?, invalid: ArrayList<Int>): ArrayList<Int> {
        val userCoupons = userCouponDao.findByUserId(session.userId)
        val couponIds = userCoupons.map { it.couponId }
        val retCouponIds = ArrayList<Int>(couponIds)
        val printStation =
                if (printStationId > 0) {
                    printStationDao.findOne(printStationId)
                } else {
                    null
                }

        //用户没有领取过的优惠券
        val couponsNotClaimed =
                if (couponIds.isEmpty()) {
                    couponDao.findAll()
                } else {
                    couponDao.findByIdNotIn(couponIds)
                }

        for (c in couponsNotClaimed) {
            val context = CouponValidateContext(
                    coupon = c,
                    user = user,
                    printStationId = printStationId,
                    positionId = printStation?.positionId ?: 0,
                    companyId = printStation?.companyId ?: 0,
                    claimMethod = CouponClaimMethod.SCAN_PRINT_STATION_CODE)

            val checkResult = validateCoupon(context,
                    this::validateCouponByClaimMethod,
                    this::validateCouponByTime,
                    this::validateCouponByMaxUses,
                    this::validateCouponByPrintStation,
                    this::validateCouponByUserRegTime)

            if (checkResult == VALID) {
                val userCoupon = UserCoupon()
                userCoupon.couponId = c.id
                userCoupon.userId = session.userId
                userCoupon.usageCount = 0
                userCoupon.claimTime = Date()
                userCouponDao.save(userCoupon)

                c.claimCount++
                couponDao.save(c)

                retCouponIds.add(c.id)
            }
        }

        //删除已禁用、不存在、过期、超过最大使用次数的优惠券
        for (userCoupon in userCoupons) {
            val coupon = couponDao.findOne(userCoupon.couponId)
            val context = CouponValidateContext(
                    coupon = coupon,
                    userCoupon = userCoupon)

            val checkResult =
                    if (coupon == null) {
                        COUPON_NOT_EXIST
                    } else {
                        validateCoupon(context,
                                this::validateCouponEnabled,
                                this::validateCouponByTime,
                                this::validateCouponByMaxUses,
                                this::validateCouponByMaxUses)
                    }

            if (checkResult != VALID) {
                //userCouponDao.delete(userCoupon)
                invalid.add(userCoupon.couponId)
                retCouponIds.remove(userCoupon.couponId)
            }
        }
        return retCouponIds
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
                    val user = userDao.findOne(session.userId)

                    synchronized(couponLock, {
                        transactionTemplate.execute {
                            claimCouponResult(code, session, user)
                        }
                    })
                }
            }
        }

    override fun claimCouponResult(code: String, session: UserLoginSession, user: User?): ClaimCouponResult {
        val coupon = couponDao.findByCodeIgnoreCase(code)
        return if (coupon == null) {
            ClaimCouponResult(1, "您输入的信息不正确，请核对后再次输入", coupon)
        } else {
            val oldUserCoupon = userCouponDao.findByUserIdAndCouponId(session.userId, coupon!!.id)
            if (oldUserCoupon != null) {
                ClaimCouponResult(1, "用户已经领取过此优惠券")
            } else {
                val context = CouponValidateContext(
                        coupon = coupon,
                        user = user)

                //XXX 用户maxUse check
                val checkResult = validateCoupon(context,
                        this::validateCouponEnabled,
                        this::validateCouponByTime,
                        this::validateCouponByMaxUses,
                        this::validateCouponByUserRegTime)

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
                "companyIdList" -> arrayOf(coupon.companyId)
                "productIdList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                "productTypeList" -> {
                    val constrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (constrains.isEmpty()) { null } else { constrains.map { it.value } }
                }
                "begin" -> {
                    if (coupon.begin == null)
                        null
                    else
                        String.format("%1\$tY-%1\$tm-%1\$td %1\$tH", coupon.begin)
                }
                "expire" -> {
                    if (coupon.expire == null)
                        null
                    else
                        String.format("%1\$tY-%1\$tm-%1\$td %1\$tH", coupon.expire)
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
        return if (coupon.maxUses == 0 || coupon.usageCount < coupon.maxUses) {
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
        return if (coupon.begin != null && now < coupon.begin!!.time) {
            NOT_BEGIN
        }
        else if (coupon.expire != null && now > coupon.expire!!.time) {
            EXPIRED
        }
        else {
            VALID
        }
    }

    override fun validateCouponEnabled(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon
        return if (!coupon.enabled) {
            DISABLED
        }
        else {
            VALID
        }
    }

    override fun validateCouponByClaimMethod(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon
        return if (coupon.claimMethod == context.claimMethod.value) {
            VALID
        }
        else {
            COUPON_NOT_EXIST
        }
    }

    override fun validateCouponByUserRegTime(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon
        val user = context.user

        val userRegConstrains = coupon.constrains.find { it.constrainsType == CouponConstrainsType.USER_REG_DAYS.value }
        return if (user == null || userRegConstrains == null) {
            VALID
        }
        else {
            val regDays = (System.currentTimeMillis() - user.createTime.timeInMillis) / (1000 * 60 * 60 * 24)
            if (regDays >= userRegConstrains.value) {
                VALID
            } else {
                USER_REG_NOT_LONG_ENOUGH
            }
        }
    }

    override fun validateCouponByPrintStation(context: CouponValidateContext): CouponValidateResult {
        val coupon = context.coupon
        val printStationId = context.printStationId
        val positionId = context.positionId

        val printStationIdConstrains = coupon.constrains
                .filter { it.constrainsType == CouponConstrainsType.PRINT_STATION.value }
                .map { it.value }

        val positionIdConstrains = coupon.constrains
                .filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                .map { it.value }

        return if (coupon.companyId == context.companyId &&
                (printStationIdConstrains.isEmpty() ||
                 printStationIdConstrains.contains(printStationId) ||
                 positionIdConstrains.contains(positionId))) {
            VALID
        } else {
            NOT_USABLE_IN_THIS_PRINT_STATION
        }
    }

    @Transactional
    override fun createCoupon(name: String, code: String,type: Int, enabled: Boolean, couponClaimMethod: CouponClaimMethod, number: Int,
                              maxUsesPerUser: Int, minExpense: Int, discount: Int, begin: Date, expire: Date, userRegDays: Int,
                              selectedProductTypes: Set<ProductType>,
                              selectedProductIds: Set<Int>,
                              selectedPositionIds: Set<Int>,
                              selectedPrintStationIds: Set<Int>) {
        val loginManager = managerService.loginManager

        val coupon = Coupon()
        coupon.name = name
        coupon.code = code
        coupon.type = type
        coupon.enabled = enabled
        coupon.companyId = loginManager!!.companyId
        coupon.claimMethod = couponClaimMethod.value
        coupon.number = number
        coupon.maxUsesPerUser = maxUsesPerUser
        coupon.minExpense = minExpense
        coupon.discount = discount
        coupon.begin = begin
        coupon.expire = expire

        couponDao.save(coupon)

        if (userRegDays > 0) {
            val cc = CouponConstrains()
            cc.constrainsType = CouponConstrainsType.USER_REG_DAYS.value
            cc.couponId = coupon.id
            cc.value = userRegDays

            couponConstrainsDao.save(cc)
        }

        couponConstrainsDao.save(
                selectedProductTypes.map {
                    val cc = CouponConstrains()
                    cc.constrainsType = CouponConstrainsType.PRODUCT_TYPE.value
                    cc.couponId = coupon.id
                    cc.value = it.value
                    cc
                })
        couponConstrainsDao.save(
                selectedProductIds.map {
                    val cc = CouponConstrains()
                    cc.constrainsType = CouponConstrainsType.PRODUCT.value
                    cc.couponId = coupon.id
                    cc.value = it
                    cc
                })
        couponConstrainsDao.save(
                selectedPositionIds.map {
                    val cc = CouponConstrains()
                    cc.constrainsType = CouponConstrainsType.POSITION.value
                    cc.couponId = coupon.id
                    cc.value = it
                    cc
                })
        couponConstrainsDao.save(
                selectedPrintStationIds.map {
                    val cc = CouponConstrains()
                    cc.constrainsType = CouponConstrainsType.PRINT_STATION.value
                    cc.couponId = coupon.id
                    cc.value = it
                    cc
                })
    }

    @Transactional
    override fun updateCoupon(id: Int, name: String, type: Int, enabled: Boolean, couponClaimMethod: CouponClaimMethod, number: Int,
                              maxUsesPerUser: Int, minExpense: Int, discount: Int, begin: Date, expire: Date, userRegDays: Int,
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
            coupon.type = type
            coupon.enabled = enabled
            coupon.claimMethod = couponClaimMethod.value
            coupon.number = number
            coupon.maxUsesPerUser = maxUsesPerUser
            coupon.minExpense = minExpense
            coupon.discount = discount
            coupon.begin = begin
            coupon.expire = expire

            couponDao.save(coupon)

            couponConstrainsDao.deleteByCouponId(coupon.id)

            if (userRegDays > 0) {
                val cc = CouponConstrains()
                cc.constrainsType = CouponConstrainsType.USER_REG_DAYS.value
                cc.couponId = coupon.id
                cc.value = userRegDays

                couponConstrainsDao.save(cc)
            }

            couponConstrainsDao.save(
                    selectedProductTypes.map {
                        val cc = CouponConstrains()
                        cc.constrainsType = CouponConstrainsType.PRODUCT_TYPE.value
                        cc.couponId = coupon.id
                        cc.value = it.value
                        cc
                    })
            couponConstrainsDao.save(
                    selectedProductIds.map {
                        val cc = CouponConstrains()
                        cc.constrainsType = CouponConstrainsType.PRODUCT.value
                        cc.couponId = coupon.id
                        cc.value = it
                        cc
                    })
            couponConstrainsDao.save(
                    selectedPositionIds.map {
                        val cc = CouponConstrains()
                        cc.constrainsType = CouponConstrainsType.POSITION.value
                        cc.couponId = coupon.id
                        cc.value = it
                        cc
                    })
            couponConstrainsDao.save(
                    selectedPrintStationIds.map {
                        val cc = CouponConstrains()
                        cc.constrainsType = CouponConstrainsType.PRINT_STATION.value
                        cc.couponId = coupon.id
                        cc.value = it
                        cc
                    })

            return true
        }
    }

    @Transactional
    override fun couponEnabled(id: Int): Boolean{

        val coupon = couponDao.findOne(id)

        if (coupon == null) {

            return false
        }

        if (coupon.enabled == false){

            coupon.enabled = true

        } else {

            coupon.enabled = false
            couponDao.save(coupon)
        }

        return true
    }




}

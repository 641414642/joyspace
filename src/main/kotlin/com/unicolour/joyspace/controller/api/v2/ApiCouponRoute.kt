package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.Coupon
import com.unicolour.joyspace.model.CouponConstrainsType
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.service.CouponService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.*

@RestController
class ApiCouponRoute {
    val logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    private lateinit var userLoginSessionDao: UserLoginSessionDao
    @Autowired
    private lateinit var userDao: UserDao
    @Autowired
    private lateinit var couponService: CouponService
    @Autowired
    private lateinit var couponDao: CouponDao

    private var couponLock = Object()

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate
    @Autowired
    private lateinit var positionDao: PositionDao
    @Autowired
    private lateinit var productDao: ProductDao

    /**
     * 返回用户优惠券列表
     */
    @GetMapping(value = "/v2/coupons")
    fun findUserCoupons(@RequestParam("sessionId") sessionId: String,
                        @RequestParam("printStationId", required = false) printStationId: Int?): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
                ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val user = userDao.findOne(session.userId)
        val printStation = printStationId ?: 0
        var couponList: List<Coupon>
        var invalidCouponList: List<Coupon>
        val couponVoList = mutableListOf<CouponVo>()
        val invalidCouponIdList = ArrayList<Int>()
        synchronized(couponLock, {
            transactionTemplate.execute {
                val couponIdList = couponService.summaryUserCouponId(session, printStation, user, invalidCouponIdList)
                couponList = couponDao.findByIdInOrderByDiscountDesc(couponIdList)
                couponVoList.addAll(couponList.map { coupon->
                    var positionList = listOf<String>()
                    var productTypeList = listOf<String>()
                    var productList = listOf<String>()
                    val positionConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    if (positionConstrains.isNotEmpty()) positionList = positionConstrains.map { positionDao.findOne(it.value).name }
                    val productTypeConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (productTypeConstrains.isNotEmpty()) productTypeList = productTypeConstrains.map { cons -> ProductType.values().first { it.value == cons.value }.dispName }
                    val productConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (productConstrains.isNotEmpty()) productList = productConstrains.map { productDao.findOne(it.value).name }
                    CouponVo(coupon.id,
                            coupon.name,
                            coupon.code,
                            coupon.begin,
                            coupon.expire,
                            coupon.minExpense,
                            coupon.discount,
                            1,
                            coupon.maxUsesPerUser,
                            positionList,
                            productTypeList,
                            productList)
                })
                invalidCouponList = couponDao.findByIdInOrderByDiscountDesc(invalidCouponIdList)
                couponVoList.addAll(invalidCouponList.map { coupon->
                    var positionList = listOf<String>()
                    var productTypeList = listOf<String>()
                    var productList = listOf<String>()
                    val positionConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    if (positionConstrains.isNotEmpty()) positionList = positionConstrains.map { positionDao.findOne(it.value).name }
                    val productTypeConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (productTypeConstrains.isNotEmpty()) productTypeList = productTypeConstrains.map { cons -> ProductType.values().first { it.value == cons.value }.dispName }
                    val productConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (productConstrains.isNotEmpty()) productList = productConstrains.map { productDao.findOne(it.value).name }
                    CouponVo(coupon.id,
                            coupon.name,
                            coupon.code,
                            coupon.begin,
                            coupon.expire,
                            coupon.minExpense,
                            coupon.discount,
                            0,
                            coupon.maxUsesPerUser,
                            positionList,
                            productTypeList,
                            productList)
                })

            }
        })
        return RestResponse.ok(couponVoList)
    }


    /**
     * 返回用户该比订单可用优惠券列表
     */
    @GetMapping(value = "/v2/coupons/order")
    fun getCouponsByOrder(@RequestParam("sessionId") sessionId: String,
                          @RequestParam("printStationId", required = false) printStationId: Int?,
                          @RequestParam("productId", required = false) productId: Int?,
                          @RequestParam("fee") fee: Int): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
                ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val user = userDao.findOne(session.userId)
        val printStation = printStationId ?: 0
        var couponList: List<Coupon>
        var invalidCouponList: List<Coupon>
        val couponVoList = mutableListOf<CouponVo>()
        val invalidCouponIdList = ArrayList<Int>()
        synchronized(couponLock, {
            transactionTemplate.execute {
                val couponIdList = couponService.summaryCouponIdByOrder(session, printStation, user, fee, invalidCouponIdList)
                couponList = couponDao.findByIdInOrderByDiscountDesc(couponIdList)
                couponVoList.addAll(couponList.map { coupon ->
                    var positionList = listOf<String>()
                    var productTypeList = listOf<String>()
                    var productList = listOf<String>()
                    val positionConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    if (positionConstrains.isNotEmpty()) positionList = positionConstrains.map { positionDao.findOne(it.value).name }
                    val productTypeConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (productTypeConstrains.isNotEmpty()) productTypeList = productTypeConstrains.map { cons -> ProductType.values().first { it.value == cons.value }.dispName }
                    val productConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (productConstrains.isNotEmpty()) productList = productConstrains.map { productDao.findOne(it.value).name }
                    CouponVo(coupon.id,
                            coupon.name,
                            coupon.code,
                            coupon.begin,
                            coupon.expire,
                            coupon.minExpense,
                            coupon.discount,
                            1,
                            coupon.maxUsesPerUser,
                            positionList,
                            productTypeList,
                            productList)
                })
                invalidCouponList = couponDao.findByIdInOrderByDiscountDesc(invalidCouponIdList)
                couponVoList.addAll(invalidCouponList.map { coupon ->
                    var positionList = listOf<String>()
                    var productTypeList = listOf<String>()
                    var productList = listOf<String>()
                    val positionConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    if (positionConstrains.isNotEmpty()) positionList = positionConstrains.map { positionDao.findOne(it.value).name }
                    val productTypeConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (productTypeConstrains.isNotEmpty()) productTypeList = productTypeConstrains.map { cons -> ProductType.values().first { it.value == cons.value }.dispName }
                    val productConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (productConstrains.isNotEmpty()) productList = productConstrains.map { productDao.findOne(it.value).name }
                    CouponVo(coupon.id,
                            coupon.name,
                            coupon.code,
                            coupon.begin,
                            coupon.expire,
                            coupon.minExpense,
                            coupon.discount,
                            0,
                            coupon.maxUsesPerUser,
                            positionList,
                            productTypeList,
                            productList)
                })

            }
        })
        return RestResponse.ok(couponVoList)
    }


    /**
     * 用户领取优惠券
     */
    @PostMapping(value = "/v2/user/claimCoupon")
    fun claimCoupon(@RequestBody param: CouponInput): RestResponse {
        val session = userLoginSessionDao.findOne(param.sessionId)
                ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val user = userDao.findOne(session.userId)
        var result = ClaimCouponResult()
        synchronized(couponLock, {
            transactionTemplate.execute {
                result = couponService.claimCouponResult(param.couponCode, session, user)
            }
        })
        return if (result.result == 0) {
            val coupon = couponDao.findOne(result.coupon!!.id)
            var positionList = listOf<String>()
            var productTypeList = listOf<String>()
            var productList = listOf<String>()
            val positionConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
            if (positionConstrains.isNotEmpty()) positionList = positionConstrains.map { positionDao.findOne(it.value).name }
            val productTypeConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
            if (productTypeConstrains.isNotEmpty()) productTypeList = productTypeConstrains.map { cons -> ProductType.values().first { it.value == cons.value }.dispName }
            val productConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
            if (productConstrains.isNotEmpty()) productList = productConstrains.map { productDao.findOne(it.value).name }
            RestResponse.ok(CouponVo(coupon.id,
                    coupon.name,
                    coupon.code,
                    coupon.begin,
                    coupon.expire,
                    coupon.minExpense,
                    coupon.discount,
                    1,
                    coupon.maxUsesPerUser,
                    positionList,
                    productTypeList,
                    productList))
        } else {
            RestResponse(result.result, null, result.description)
        }

    }


    /**
     * 用户领取优惠券
     */
    @PostMapping(value = "/v2/user/claimCoupon/auto")
    fun claimAutoCoupon(@RequestBody param: CouponAutoInput): RestResponse {
        val session = userLoginSessionDao.findOne(param.sessionId)
                ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val user = userDao.findOne(session.userId)
        val printStation = param.printStationId
        var couponList: List<Coupon>
        val couponVoList = mutableListOf<CouponVo>()
        synchronized(couponLock, {
            transactionTemplate.execute {
                val couponIdList = couponService.claimAutoCoupon(session, printStation, user)
                couponList = couponDao.findByIdInOrderByDiscountDesc(couponIdList)
                couponVoList.addAll(couponList.map { coupon->
                    var positionList = listOf<String>()
                    var productTypeList = listOf<String>()
                    var productList = listOf<String>()
                    val positionConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    if (positionConstrains.isNotEmpty()) positionList = positionConstrains.map { positionDao.findOne(it.value).name }
                    val productTypeConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    if (productTypeConstrains.isNotEmpty()) productTypeList = productTypeConstrains.map { cons -> ProductType.values().first { it.value == cons.value }.dispName }
                    val productConstrains = coupon.constrains.filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    if (productConstrains.isNotEmpty()) productList = productConstrains.map { productDao.findOne(it.value).name }

                    CouponVo(coupon.id,
                            coupon.name,
                            coupon.code,
                            coupon.begin,
                            coupon.expire,
                            coupon.minExpense,
                            coupon.discount,
                            1,
                            coupon.maxUsesPerUser,
                            positionList,
                            productTypeList,
                            productList)
                })
            }
        })
        return RestResponse.ok(couponVoList)

    }

}
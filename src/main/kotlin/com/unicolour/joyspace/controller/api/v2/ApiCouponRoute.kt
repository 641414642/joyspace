package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.CouponDao
import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.ClaimCouponResult
import com.unicolour.joyspace.dto.CouponVo
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.Coupon
import com.unicolour.joyspace.service.CouponService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
        var couponList = listOf<Coupon>()
        synchronized(couponLock, {
            transactionTemplate.execute {
                val couponIdList = couponService.summaryUserCouponId(session, printStation, user)
                couponList = couponDao.findByIdInOrderByDiscountDesc(couponIdList)
            }
        })
        return RestResponse.ok(couponList)
    }


    /**
     * 返回用户该比订单可用优惠券列表
     */
    @GetMapping(value = "/v2/coupons/order")
    fun getCouponsByOrder(): RestResponse {
        val coupons = mutableListOf<CouponVo>()
        return RestResponse.ok(coupons)
    }


    /**
     * 用户领取优惠券
     */
    @PostMapping(value = "/v2/user/claimCoupon")
    fun claimCoupon(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("couponCode") couponCode: String): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
                ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val user = userDao.findOne(session.userId)
        var result = ClaimCouponResult()
        synchronized(couponLock, {
            transactionTemplate.execute {
                result = couponService.claimCouponResult(couponCode, session, user)
            }
        })
        return if (result.result == 0) {
            val coupon = couponDao.findOne(result.coupon!!.id)
            RestResponse.ok(coupon)
        } else {
            RestResponse(result.result, null, result.description)
        }

    }

}
package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.CouponDao
import com.unicolour.joyspace.dao.UserCouponDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.UserCouponListResult
import com.unicolour.joyspace.service.CouponService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CouponServiceImpl : CouponService {
    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var userCouponDao: UserCouponDao

    @Autowired
    lateinit var couponDao: CouponDao

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
}

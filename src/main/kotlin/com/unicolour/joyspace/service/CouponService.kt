package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.UserCouponListResult
import graphql.schema.DataFetcher

interface CouponService {
    val userCouponListDataFetcher: DataFetcher<UserCouponListResult>
}
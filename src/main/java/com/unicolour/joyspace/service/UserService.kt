package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.AppUserLoginResult
import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.UserDTO
import com.unicolour.joyspace.dto.WxLoginResult
import com.unicolour.joyspace.model.User
import graphql.schema.DataFetcher

interface UserService {
    fun createOrUpdateUser(user: UserDTO): User
    fun login(userName: String?, phoneNumber:String?, password:String): AppUserLoginResult

    fun getLoginDataFetcher(): DataFetcher<AppUserLoginResult>

    fun getSendRegVerifyCodeDataFetcher(): DataFetcher<GraphQLRequestResult>
    fun getUserRegisterDataFetcher(): DataFetcher<GraphQLRequestResult>

    //微信用户登录，返回 sessionId
    fun wxLogin(code: String): WxLoginResult
}


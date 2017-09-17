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

    val loginDataFetcher: DataFetcher<AppUserLoginResult>

    val sendRegVerifyCodeDataFetcher: DataFetcher<GraphQLRequestResult>

    /** 用户注册 */
    val userRegisterDataFetcher: DataFetcher<GraphQLRequestResult>

    /** 请求重置密码 */
    val requestResetPasswordDataFetcher: DataFetcher<GraphQLRequestResult>

    /** 重置密码 */
    val resetPasswordDataFetcher: DataFetcher<GraphQLRequestResult>

    //微信用户登录，返回 sessionId
    fun wxLogin(code: String): WxLoginResult
}


package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.UserDTO
import com.unicolour.joyspace.model.User
import graphql.schema.DataFetcher

interface UserService {
    fun createOrUpdateUser(user: UserDTO): User
    fun login(phoneNumber:String, password:String): User?

    fun getLoginDataFetcher(): DataFetcher<User>
    fun getSendRegVerifyCodeDataFetcher(): DataFetcher<String>

    fun getAuthTokenDataFetcher(): DataFetcher<String>
    fun getUserRegisterDataFetcher(): DataFetcher<String>
}
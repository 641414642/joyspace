package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import org.springframework.security.core.userdetails.UserDetailsService

interface ManagerService : UserDetailsService {
    fun createManager(userName: String, password: String, fullName: String, cellPhone: String, email: String, company: Company): Manager
    fun resetPassword(userId: Int, password: String): Boolean
    fun login(userName: String, password: String): Manager?
    val loginManager: LoginManagerDetail?
    fun createManagerBindKey(): String
    fun bindWeiXinAccount(bindKey: String, code: String): Boolean
}

package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.dto.WxLoginResult
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import org.springframework.security.core.userdetails.UserDetailsService

interface ManagerService : UserDetailsService {
    fun createManager(userName: String, password: String, fullName: String, cellPhone: String,
                      email: String, roles: String, company: Company): Manager
    fun resetPassword(userId: Int, password: String): Boolean
    fun login(userName: String, password: String): Manager?
    val loginManager: LoginManagerDetail?
    fun loginManagerHasRole(role: String): Boolean

    fun getCompanyManager(companyId: Int): Manager?
}

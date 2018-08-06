package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dao.ManagerWxLoginSessionDao
import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.WxLoginResult
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.model.ManagerWxLoginSession
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.RestTemplate
import java.util.*
import kotlin.collections.HashMap

@Service
open class ManagerServiceImpl : ManagerService {
    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var managerWxLoginSessionDao: ManagerWxLoginSessionDao

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var objectMapper: ObjectMapper

    override fun getCompanyManager(companyId: Int): Manager? {
        return managerDao.findByCompanyId(companyId).minBy { it.id }
    }

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val manager = managerDao.findByUserName(username)
        return if (manager != null) {
            LoginManagerDetail(
                    manager.id,
                    manager.companyId,
                    manager.company.name,
                    manager.createTime,
                    manager.fullName,
                    manager.userName,
                    manager.password,
                    manager.isEnabled,
                    true,
                    true,
                    true,
                    manager.roles.split(',').map { SimpleGrantedAuthority("ROLE_$it") })
        } else {
            throw UsernameNotFoundException(username + " not found.")
        }
    }

    override fun createManager(userName: String,
                               password: String,
                               fullName: String,
                               cellPhone: String,
                               email: String,
                               roles: String,
                               company: Company): Manager {
        if (managerDao.existsByUserNameIgnoreCase(userName)) {
            throw ProcessException(ResultCode.MANAGER_ALREADY_EXISTS, "名称为${userName}的管理员已存在")
        }

        val manager = Manager()

        manager.userName = userName
        manager.fullName = fullName
        manager.password = passwordEncoder.encode(password)
        manager.cellPhone = cellPhone
        manager.email = email
        manager.createTime = Calendar.getInstance()
        manager.isEnabled = true
        manager.roles = roles
        manager.company = company

        managerDao.save(manager)

        //operationLogService.addOperationLog("创建用户：" + manager.getUserName());

        return manager
    }

    override fun loginManagerHasRole(role: String): Boolean {
        return (loginManager?.authorities ?: emptyList()).map { it.authority }.contains(role)
    }

    override fun resetPassword(userId: Int, password: String): Boolean {
        val manager = managerDao.findOne(userId)

        if (manager != null) {
            manager.password = passwordEncoder.encode(password)
            managerDao.save(manager)

            //operationLogService.addOperationLog("重置密码：" + manager.getUserName());
            return true
        } else {
            return false
        }
    }

    override fun login(userName: String, password: String): Manager? {
        val manager = managerDao.findByUserName(userName)
        if (manager != null) {
            if (passwordEncoder.matches(password, manager.password)) {
                return manager
            }
        }

        return null
    }

    override val loginManager: LoginManagerDetail?
        get() {
            val auth = SecurityContextHolder.getContext().authentication
            return if (auth != null && auth.principal is LoginManagerDetail) {
                auth.principal as LoginManagerDetail
            } else {
                null
            }
        }
}

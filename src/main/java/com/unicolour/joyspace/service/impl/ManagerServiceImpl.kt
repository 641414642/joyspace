package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class ManagerServiceImpl : ManagerService {
    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val manager = managerDao.findByUserNameOrFullName(username, username)
        return if (manager != null) {
            LoginManagerDetail(
                    manager.id,
                    manager.companyId,
                    manager.createTime,
                    manager.fullName,
                    manager.userName,
                    manager.password,
                    manager.isEnabled,
                    true, true, true, Arrays.asList(SimpleGrantedAuthority("USER")))
        } else {
            throw UsernameNotFoundException(username + " not found.")
        }
    }

    override fun createManager(userName: String, password: String, fullName: String, cellPhone: String, email: String, company: Company): Manager {
        val manager = Manager()

        manager.userName = userName
        manager.fullName = fullName
        manager.password = passwordEncoder.encode(password)
        manager.cellPhone = cellPhone
        manager.email = email
        manager.createTime = Calendar.getInstance()
        manager.isEnabled = true
        manager.company = company

        managerDao.save(manager)

        //operationLogService.addOperationLog("创建用户：" + manager.getUserName());

        return manager
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

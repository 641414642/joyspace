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
    @Value("\${com.unicolour.wxManagerAppId}")
    lateinit var wxManagerAppId: String

    @Value("\${com.unicolour.wxManagerAppSecret}")
    lateinit var wxManagerAppSecret: String

    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var managerWxLoginSessionDao: ManagerWxLoginSessionDao

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    private val managerIdBindKeyMap: MutableMap<Int, String> = Collections.synchronizedMap(HashMap())

    override fun createManagerBindKey(): String {
        val manager = loginManager

        return if (manager != null) {
            val bindKey = """${manager.managerId}/${UUID.randomUUID().toString().replace("-", "").substring(8)}"""
            managerIdBindKeyMap[manager.managerId] = bindKey
            bindKey
        }
        else {
            ""
        }
    }

    override fun managerWeiXinLogin(bindKey: String?, code: String): WxLoginResult {
        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={js_code}&grant_type={grant_type}",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "appid" to wxManagerAppId,
                        "secret" to wxManagerAppSecret,
                        "js_code" to code,
                        "grant_type" to "authorization_code"
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val bodyStr = resp.body
            val body: JSCode2SessionResult = objectMapper.readValue(bodyStr, JSCode2SessionResult::class.java)

            if (body.errcode == 0 && body.openid != null && body.session_key != null) {
                val manager = managerDao.findByWxOpenId(body.openid!!)
                if (manager == null) {  //微信账号没有绑定管理员
                    if (bindKey.isNullOrBlank()) {
                        return WxLoginResult(1, "WeiXin account not bind to manager")
                    } else {
                        //XXX 验证后面的部分
                        val managerId = bindKey!!.substringBefore('/').toInt()
                        val bindManager = managerDao.findOne(managerId)
                        if (bindManager == null) {
                            return WxLoginResult(2, "Bind WeiXin account failed, manager(id=$managerId) not found")
                        } else {
                            return transactionTemplate.execute {
                                bindManager.wxOpenId = body.openid
                                managerDao.save(bindManager)

                                val session = createManagerWxLoginSession(bindManager, body)
                                WxLoginResult(0, "Bind WeiXin account and login success", session.id)
                            }
                        }
                    }
                } else {   //微信账号已绑定管理员
                    return transactionTemplate.execute {
                        val session = createManagerWxLoginSession(manager, body)
                        WxLoginResult(sessionId = session.id)
                    }
                }
            }
        }

        return WxLoginResult(3, "WeiXin login request failed")
    }

    private fun createManagerWxLoginSession(manager: Manager, body: JSCode2SessionResult): ManagerWxLoginSession {
        var session = managerWxLoginSessionDao.findByManagerId(manager.id)
        if (session == null) {
            session = ManagerWxLoginSession()
            session.id = UUID.randomUUID().toString().replace("-", "")
            session.managerId = manager.id
        }

        session.wxSessionKey = body.session_key!!
        session.wxOpenId = body.openid!!

        session.expireTime = Calendar.getInstance()
        session.expireTime.add(Calendar.SECOND, 3600)

        managerWxLoginSessionDao.save(session)
        return session
    }

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

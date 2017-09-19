package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dao.VerifyCodeDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.SmsService
import com.unicolour.joyspace.service.UserService
import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.RestTemplate
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.transaction.Transactional


@Service
open class UserServiceImpl : UserService {
    companion object {
        val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }

    @Value("\${com.unicolour.wxAppId}")
    lateinit var wxAppId: String

    @Value("\${com.unicolour.wxAppSecret}")
    lateinit var wxAppSecret: String

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var verifyCodeDao: VerifyCodeDao

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var smsService: SmsService

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    //登录
    override val loginDataFetcher: DataFetcher<AppUserLoginResult>
        get() {
            return DataFetcher<AppUserLoginResult> { env ->
                val phoneNumber = env.getArgument<String>("phoneNumber")
                val userName = env.getArgument<String>("userName")
                val password = env.getArgument<String>("password")
                transactionTemplate.execute { login(userName, phoneNumber, password) }
            }
        }

    //发送注册验证码
    override val sendRegVerifyCodeDataFetcher: DataFetcher<GraphQLRequestResult>
        get() {
            return DataFetcher<GraphQLRequestResult> { env ->
                val phoneNumber = env.getArgument<String>("phoneNumber")
                transactionTemplate.execute { sendVerifyCode(phoneNumber, true) }
            }
        }

    private fun sendVerifyCode(phoneNumber: String, regCode: Boolean): GraphQLRequestResult {
        val codeType = if (regCode) "帐号绑定" else "重置密码"
        val smsTpl = "【优利绚彩】${codeType}验证码为:%s,请勿向任何人提供您收到的短信验证码。"

        val now = Instant.now()
        var verifyCode = verifyCodeDao.findOne(phoneNumber)
        if (verifyCode != null) {
            val interval = Duration.between(verifyCode.sendTime.toInstant(), now);
            if (interval.seconds < 60) {
                return GraphQLRequestResult(ResultCode.RETRY_LATER)
            }
            else {
                verifyCode.code = String.format("%06d", secureRandom.nextInt(1000000))
                verifyCode.sendTime = Calendar.getInstance()
                verifyCodeDao.save(verifyCode)

                val sendResult = smsService.send(phoneNumber, String.format(smsTpl, verifyCode.code))
                if (sendResult.first != 3) {
                    logger.error("Send SMS error, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                }
                else {
                    logger.info("Send SMS success, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                }
            }
        }
        else {
            val user = userDao.findByPhone(phoneNumber)
            if (regCode && user != null) {
                return GraphQLRequestResult(ResultCode.PHONE_NUMBER_ALREADY_REGISTERED)
            }
            else if (!regCode && user == null) {
                return GraphQLRequestResult(ResultCode.USER_NOT_FOUND_FOR_THIS_PHONE_NUMBER)
            }
            else {
                verifyCode = VerifyCode()
                verifyCode.phoneNumber = phoneNumber
                verifyCode.code = String.format("%06d", secureRandom.nextInt(1000000))
                verifyCode.sendTime = Calendar.getInstance()

                verifyCodeDao.save(verifyCode)

                val sendResult = smsService.send(phoneNumber, String.format(smsTpl, verifyCode.code))
                if (sendResult.first != 3) {
                    logger.error("Send SMS error, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                }
                else {
                    logger.info("Send SMS success, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                }
            }
        }
        //XXX
        return GraphQLRequestResult(ResultCode.SUCCESS)

//        #发送失败
//        FAILED
//
//        #服务器错误
//        SERVER_ERROR

    }

    override val userRegisterDataFetcher: DataFetcher<GraphQLRequestResult>
        get() {
            return DataFetcher { env ->
                val phoneNumber = env.getArgument<String>("phoneNumber")
                val userName = env.getArgument<String>("userName")
                val password = env.getArgument<String>("password")
                val verifyCode = env.getArgument<String>("verifyCode")
                val email = env.getArgument<String>("email")

                userRegister(userName, password, phoneNumber, verifyCode, email)
            }
        }

    override val requestResetPasswordDataFetcher: DataFetcher<GraphQLRequestResult>
        get() {
            return DataFetcher { env ->
                val phoneNumber = env.getArgument<String>("phoneNumber")
                sendVerifyCode(phoneNumber, false)
            }
        }

    override val resetPasswordDataFetcher: DataFetcher<GraphQLRequestResult>
        get() {
            return DataFetcher { env ->
                val userName = env.getArgument<String>("userName")
                val phoneNumber = env.getArgument<String>("phoneNumber")
                val verifyCode = env.getArgument<String>("verifyCode")
                val newPassword = env.getArgument<String>("newPassword")

                resetPassword(userName, phoneNumber, verifyCode, newPassword)
            }
        }

    //重置密码
    private fun resetPassword(userName: String?, phoneNumber: String?,
                              verifyCode: String, newPassword: String): GraphQLRequestResult {
        val now = Instant.now()
        var user: User? = null
        if (userName != null) {
            user = userDao.findByUserName(userName)
        }
        else if (phoneNumber != null) {
            user = userDao.findByPhone(phoneNumber)
        }

        if (user == null) {
            return GraphQLRequestResult(ResultCode.USER_NOT_FOUND)
        }
        else {
            val verifyCodeObj = verifyCodeDao.findOne(user.phone)
            if (verifyCodeObj == null ||
                    verifyCodeObj.code != verifyCode ||
                    Duration.between(verifyCodeObj.sendTime.toInstant(), now).seconds > 60 * 10) {  //超过10分钟
                return GraphQLRequestResult(ResultCode.INVALID_VERIFY_CODE)
            }
            else {
                user.password = passwordEncoder.encode(newPassword)  //XXX 检查密码有效性

                userDao.save(user)
                verifyCodeDao.delete(verifyCodeObj)

                return GraphQLRequestResult(ResultCode.SUCCESS)
            }
        }
    }

    //用户注册
    private fun userRegister(userName: String, password: String,
                             phoneNumber: String, verifyCode: String, email: String?): GraphQLRequestResult {
        val now = Instant.now()
        val verifyCodeObj = verifyCodeDao.findOne(phoneNumber)
        if (verifyCodeObj == null ||
                verifyCodeObj.code != verifyCode ||
                Duration.between(verifyCodeObj.sendTime.toInstant(), now).seconds > 60 * 10) {  //超过10分钟
            return GraphQLRequestResult(ResultCode.INVALID_VERIFY_CODE)
        }
        else {
            var user = userDao.findByPhone(phoneNumber)
            if (user != null) {
                return GraphQLRequestResult(ResultCode.PHONE_NUMBER_ALREADY_REGISTERED)
            }

            user = userDao.findByUserName(userName)
            if (user != null) {
                return GraphQLRequestResult(ResultCode.USER_NAME_ALREADY_REGISTERED)
            }

            user = User()
            user.userName = userName
            user.password = passwordEncoder.encode(password)
            user.phone = phoneNumber
            user.email = email
            user.enabled = true
            user.sex = USER_SEX_UNKNOWN
            user.createTime = Calendar.getInstance()

            userDao.save(user)
            verifyCodeDao.delete(verifyCodeObj)

            return GraphQLRequestResult(ResultCode.SUCCESS)
        }
//XXX
//        #服务器错误
//        SERVER_ERROR
    }

    //app用户登录
    private fun login(userName: String?, phoneNumber:String?, password: String): AppUserLoginResult {
            val user =
            if (!phoneNumber.isNullOrEmpty()) {
                userDao.findByPhone(phoneNumber!!)
            }
            else if (!userName.isNullOrEmpty()) {
                userDao.findByUserName(userName!!)
            }
            else {
                null
            }

        if (user != null) {
            if (passwordEncoder.matches(password, user.password)) {
                var session = userLoginSessionDao.findByUserId(user.id)
                if (session == null) {
                    session = UserLoginSession()
                    session.id = UUID.randomUUID().toString().replace("-", "")
                    session.userId = user.id
                }

                session.expireTime = Calendar.getInstance()
                session.expireTime.add(Calendar.SECOND, 3600)
                userLoginSessionDao.save(session)

                return AppUserLoginResult(session = AppUserLoginSession(session.id, userInfo = user))
            }
            else {
                if (!phoneNumber.isNullOrEmpty()) {
                    return AppUserLoginResult(result = 1, description = "手机号或密码错误")
                }
                else {
                    return AppUserLoginResult(result = 2, description = "用户名或密码错误")
                }
            }
        }

        if (!phoneNumber.isNullOrEmpty()) {
            return AppUserLoginResult(result = 1, description = "手机号或密码错误")
        }
        else if (!userName.isNullOrEmpty()) {
            return AppUserLoginResult(result = 2, description = "用户名或密码错误")
        }
        else {
            return AppUserLoginResult(result = 3, description = "请输入用户名或手机号")
        }
    }

    //微信用户登录
    @Transactional
    override fun wxLogin(code: String): WxLoginResult {
        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/jscode2session" +
                        "?appid={appid}&secret={secret}&js_code={js_code}&grant_type={grant_type}",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "appid" to wxAppId,
                        "secret" to wxAppSecret,
                        "js_code" to code,
                        "grant_type" to "authorization_code"
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val bodyStr = resp.body
            val body: JSCode2SessionResult = objectMapper.readValue(bodyStr, JSCode2SessionResult::class.java)

            if (body.errcode == 0 && body.openid != null && body.session_key != null) {
                var user = userDao.findByWxOpenId(body.openid!!)
                if (user == null) {
                    user = User()
                    user.userName = body.openid
                    user.wxOpenId = body.openid
                    user.password = passwordEncoder.encode(body.openid)
                    user.phone = null
                    user.email = null
                    user.enabled = true
                    user.sex = USER_SEX_UNKNOWN
                    user.createTime = Calendar.getInstance()
                    userDao.save(user)
                }

                var session = userLoginSessionDao.findByUserId(user.id)
                if (session == null) {
                    session = UserLoginSession()
                    session.id = UUID.randomUUID().toString().replace("-", "")
                    session.userId = user.id
                }

                session.wxSessionKey = body.session_key!!
                session.wxOpenId = body.openid!!

                session.expireTime = Calendar.getInstance()
                session.expireTime.add(Calendar.SECOND, 3600)

                userLoginSessionDao.save(session)

                return WxLoginResult(sessionId = session.id)
            } else {
                return WxLoginResult(errcode = body.errcode, errmsg = body.errmsg)
            }
        }

        return WxLoginResult(errcode = -1, errmsg = "")
    }

    @Autowired
    lateinit var userDao: UserDao

    override fun createOrUpdateUser(user: UserDTO): User {
        var retUser:User? = null

        if (!user.wxOpenId.isNullOrEmpty()) {
            retUser = userDao.findByWxOpenId(user.wxOpenId!!)
        }

        if (retUser == null) {
            retUser = User()
            retUser.createTime = Calendar.getInstance()
            retUser.enabled = true
        }

        retUser.email = user.email
        retUser.userName = user.userName
        retUser.wxOpenId = user.wxOpenId
        retUser.fullName = user.fullName
        retUser.sex = when (user.sex) {
            "M" -> USER_SEX_MALE
            "F" -> USER_SEX_FEMALE
            else -> USER_SEX_UNKNOWN
        }
        retUser.email = user.email
        retUser.phone = user.phone

        userDao.save(retUser)

        return retUser
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class JSCode2SessionResult(
        var openid: String? = null,
        var session_key: String? = null,
        var expires_in: String? = null,
        var errcode: Int? = 0,
        var errmsg: String? = null
)
package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.UserDTO
import com.unicolour.joyspace.model.USER_SEX_FEMALE
import com.unicolour.joyspace.model.USER_SEX_MALE
import com.unicolour.joyspace.model.USER_SEX_UNKNOWN
import com.unicolour.joyspace.model.User
import com.unicolour.joyspace.service.UserService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import sun.security.provider.certpath.BuildStep.SUCCEED
import java.util.*

@Service
class UserServiceImpl : UserService {
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    private val tokenUserIdMap: MutableMap<String, Int> = HashMap()
    private val userIdTokenMap: MutableMap<Int, String> = HashMap()

    //登录
    override fun getLoginDataFetcher(): DataFetcher<User> {
        return DataFetcher<User> { env ->
            val phoneNumber = env.getArgument<String>("phoneNumber")
            val password = env.getArgument<String>("password")
            login(phoneNumber, password)
        }
    }

    //发送注册验证码
    override fun getSendRegVerifyCodeDataFetcher(): DataFetcher<GraphQLRequestResult> {
        return DataFetcher<GraphQLRequestResult> { env ->
            val phoneNumber = env.getArgument<String>("phoneNumber")
            sendVerifyCode(phoneNumber)
        }
    }

    private fun sendVerifyCode(phoneNumber: String): GraphQLRequestResult {
        //XXX
        return GraphQLRequestResult(ResultCode.SUCCESS)

//        #发送失败
//        FAILED
//
//        #请求太频繁(请等待60秒以后再试)
//        RETRY_LATER
//
//        #手机号码已经注册过
//        PHONE_NUMBER_ALREADY_REGISTERED
//
//        #服务器错误
//        SERVER_ERROR

    }


    override fun getUserRegisterDataFetcher(): DataFetcher<GraphQLRequestResult> {
        return DataFetcher<GraphQLRequestResult> { env ->
            val phoneNumber = env.getArgument<String>("phoneNumber")
            val userName = env.getArgument<String>("userName")
            val password = env.getArgument<String>("password")
            val verifyCode = env.getArgument<String>("verifyCode")
            val email = env.getArgument<String>("email")

            userRegister(userName, password, phoneNumber, verifyCode, email)
        }
    }

    //用户注册
    private fun userRegister(userName: String, password: String,
                             phoneNumber: String, verifyCode: String, email: String?): GraphQLRequestResult {
        if (verifyCode != "123456") {
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

            return GraphQLRequestResult(ResultCode.SUCCESS)
        }
//XXX
//        #注册成功
//        SUCCEED
//
//        #手机号码已经注册过
//        PHONE_NUMBER_ALREADY_REGISTERED
//
//        #用户名已经注册过
//        USER_NAME_ALREADY_REGISTERED
//
//        #无效或过期的验证码
//        INVALID_VERIFY_CODE
//
//        #服务器错误
//        SERVER_ERROR
    }

    override fun getAuthTokenDataFetcher(): DataFetcher<String> {
        return DataFetcher<String> { env ->
            val user = env.getSource<User>()
            userIdTokenMap[user.id]
        }
    }

    override fun login(phoneNumber: String, password: String): User? {
        val user = userDao.findByPhone(phoneNumber)
        if (user != null) {
            if (passwordEncoder.matches(password, user.password)) {
                if (userIdTokenMap.containsKey(user.id)) {
                    val oldToken = userIdTokenMap[user.id]
                    tokenUserIdMap.remove(oldToken)
                    userIdTokenMap.remove(user.id)
                }

                val token = UUID.randomUUID().toString().replace("-", "")
                tokenUserIdMap[token] = user.id
                userIdTokenMap[user.id] = token

                return user
            }
        }

        return null
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


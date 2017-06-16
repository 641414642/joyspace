package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dto.UserDTO
import com.unicolour.joyspace.model.User
import com.unicolour.joyspace.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserServiceImpl : UserService {
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
            "M" -> 1.toByte()
            "F" -> 2.toByte()
            else -> 0.toByte()
        }
        retUser.email = user.email
        retUser.phone = user.phone

        userDao.save(retUser)

        return retUser
    }
}


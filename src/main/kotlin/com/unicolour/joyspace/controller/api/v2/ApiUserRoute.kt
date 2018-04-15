package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.UserAddressDao
import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.Address
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ApiUserRoute {

    val logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    private lateinit var userAddressDao: UserAddressDao
    @Autowired
    private lateinit var userLoginSessionDao: UserLoginSessionDao
    @Autowired
    private lateinit var userDao: UserDao

    //获取个人信息


    /**
     * 获取用户地址列表
     */
    @GetMapping(value = "/v2/user/address")
    fun listAddress(@RequestParam("sessionId") sessionId: String): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
        val user = userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val addressList = userAddressDao.findByUserId(user.id)
        return RestResponse.ok(mapOf("addressList" to addressList))
    }


    /**
     * 用户新增地址
     */
    @PostMapping(value = "/v2/user/address")
    fun add(@RequestParam("sessionId") sessionId: String,
            @RequestParam("sessionId") province: String,
            @RequestParam("sessionId") city: String,
            @RequestParam("sessionId") area: String,
            @RequestParam("sessionId") address: String,
            @RequestParam("sessionId") phoneNum: String,
            @RequestParam("sessionId") name: String,
            @RequestParam("sessionId") default: Int?): RestResponse {
        val session = userLoginSessionDao.findOne(sessionId)
        val user = userDao.findOne(session.userId) ?: return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        val addr = Address()
        addr.userId = user.id
        addr.province = province
        addr.city = city
        addr.area = area
        addr.address = address
        addr.phoneNum = phoneNum
        addr.name = name
        if (default == 1) addr.defalut = true
        addr.createTime = Calendar.getInstance()
        userAddressDao.save(addr)
        return RestResponse.ok()
    }
}
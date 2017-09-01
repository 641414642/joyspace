package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dto.WxLoginResult
import com.unicolour.joyspace.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiUserController {
    @Autowired
    lateinit var userService: UserService

    @RequestMapping("/api/user/login", method = arrayOf(RequestMethod.POST))
    fun wxUserLogin(@RequestParam("code") code: String) : ResponseEntity<WxLoginResult> {
        val result = userService.wxLogin(code)
        return ResponseEntity.ok(result)
    }
}

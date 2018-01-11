package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.WxLoginResult
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ApiManagerController {
    @Autowired
    lateinit var managerService: ManagerService

    @RequestMapping("/api/manager/login", method = arrayOf(RequestMethod.POST))
    fun wxManagerLogin(@RequestParam("code") code: String, @RequestParam("bindKey") bindKey: String) : ResponseEntity<WxLoginResult> {
        val result = managerService.managerWeiXinLogin(bindKey, code)
        return ResponseEntity.ok(result)
    }
}

package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.UserDao
import com.unicolour.joyspace.dto.UserDTO
import com.unicolour.joyspace.dto.userToDTO
import com.unicolour.joyspace.model.User
import com.unicolour.joyspace.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
class UserController {
    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var userService: UserService

    @RequestMapping("/api/user", method = arrayOf(RequestMethod.POST))
    fun createOrEditUser(@RequestBody user: UserDTO) : ResponseEntity<UserDTO> {
        val retUser: User = userService.createOrUpdateUser(user)
        return ResponseEntity.ok(retUser.userToDTO())
    }

    @RequestMapping("/api/user/findByOpenId", method = arrayOf(RequestMethod.GET))
    fun findUserByWeiXinOpenId(@RequestParam("openId") openId: String) : ResponseEntity<UserDTO> {
        val retUser: User? = userDao.findByWxOpenId(openId)
        if (retUser == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            return ResponseEntity.ok(retUser.userToDTO())
        }
    }
}

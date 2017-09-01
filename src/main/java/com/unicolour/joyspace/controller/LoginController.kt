package com.unicolour.joyspace.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
class LoginController {

    @RequestMapping("/login")
    fun index(@RequestParam(name = "error", required = false) error: String?): ModelAndView {
        var errorMsg: String? = null
        if (error != null) {  //登录失败
            errorMsg = "用户名或密码错误!"
        }

        logger.info("/login, errorMsg={}", errorMsg)

        return ModelAndView("login", "errorMsg", errorMsg)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoginController::class.java)
    }
}
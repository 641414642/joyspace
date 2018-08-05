package com.unicolour.joyspace.controller

import com.unicolour.joyspace.service.PrintStationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
class LoginController {
    @Autowired
    lateinit var printStationService: PrintStationService

    @RequestMapping("/login")
    fun index(
            modelAndView: ModelAndView,
            @RequestParam(name = "error", required = false) error: String?): ModelAndView {
        var errorMsg: String? = null
        if (error != null) {  //登录失败
            errorMsg = "用户名或密码错误!"
        }

        logger.info("/login, errorMsg={}", errorMsg)

        modelAndView.viewName = "login"
        modelAndView.model["errorMsg"] = errorMsg
        modelAndView.model["homeUrl"] = printStationService.getHomeDownloadUrl()

        return modelAndView
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoginController::class.java)
    }

    @RequestMapping("/register")
    fun register(@RequestParam(name = "error", required = false) error: String?): ModelAndView {
        return ModelAndView("register")
    }
    @RequestMapping("/forget")
    fun forget(@RequestParam(name = "error", required = false) error: String?): ModelAndView {
        return ModelAndView("forget")
    }
}
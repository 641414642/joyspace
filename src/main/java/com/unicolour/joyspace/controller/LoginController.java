package com.unicolour.joyspace.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping("/login")
    public ModelAndView index(@RequestParam(name="error", required=false) String error) {
		String errorMsg = null;
		if (error != null) {  //登录失败
			errorMsg = "用户名或密码错误!";
		}

		logger.info("/login, errorMsg={}", errorMsg);

		return new ModelAndView("login", "errorMsg", errorMsg);
    }
}
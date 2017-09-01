package com.unicolour.joyspace.controller.api

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RestController
class TestController {
    @RequestMapping("/wx/**", method = arrayOf(RequestMethod.POST, RequestMethod.GET))
    @ResponseBody
    fun wxTest(request: HttpServletRequest) : Any{
        request.parameterNames.iterator().forEach {
            println(it + "=" + request.getParameter(it))
        }

        return "OK"
    }
}

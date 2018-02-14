package com.unicolour.joyspace.config

import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.util.format
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.WebUtils
import java.lang.Exception
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SystemHandlerInterceptor : HandlerInterceptor{
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any?): Boolean {
        return true
    }

    override fun postHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any?, modelAndView: ModelAndView?) {
        if (modelAndView?.viewName == "layout") {
            val sideBarCollapsed = WebUtils.getCookie(request, "sideBarCollapsed")?.value == "true"
            modelAndView.model["sideBarCollapsed"] = sideBarCollapsed

            val auth: Authentication? = SecurityContextHolder.getContext().authentication
            val userDetail = auth?.principal

            if (userDetail is LoginManagerDetail) {
                val dispName = if (userDetail.fullName.isNullOrEmpty()) auth.name else userDetail.fullName
                val regTime = userDetail.createTime.format()

                modelAndView.model["LoginManagerCompanyName"] = userDetail.companyName
                modelAndView.model["LoginManagerDispName"] = dispName
                modelAndView.model["LoginManagerName"] = auth.name
                modelAndView.model["LoginManagerRegTime"] = regTime
                modelAndView.model["LoginManagerRoles"] = userDetail.authorities.map { it.authority }
            }

        }
    }

    override fun afterCompletion(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?, ex: Exception?) {
    }
}
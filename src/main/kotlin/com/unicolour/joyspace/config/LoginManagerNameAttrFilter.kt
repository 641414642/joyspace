package com.unicolour.joyspace.config

import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.util.format
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*

@Component
class LoginManagerNameAttrFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val auth: Authentication? = SecurityContextHolder.getContext().authentication

        val userDetail = auth?.principal

        if (userDetail is LoginManagerDetail) {
            val dispName = if (userDetail.fullName.isNullOrEmpty()) auth.name else userDetail.fullName
            val regTime = userDetail.createTime.format()

            req.setAttribute("LoginUserDispName", dispName)
            req.setAttribute("LoginUserName", auth.name)
            req.setAttribute("LoginUserRegTime", regTime)
        }
        chain.doFilter(req, res)
    }

    override fun destroy() {}

    @Throws(ServletException::class)
    override fun init(cfg: FilterConfig) {
    }
}
package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.util.Pager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView

@Controller
class ManagerController {

    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var managerService: ManagerService

    @RequestMapping("/manager/list")
    fun adminUserList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        //		String reqPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val pageable = PageRequest(pageno - 1, 20)
        val users = if (name == null || name == "")
            managerDao.findByCompanyId(loginManager.companyId, pageable)
        else
            managerDao.findByCompanyIdAndUserNameOrFullName(loginManager.companyId, name, pageable)

        modelAndView.model.put("inputUserName", name)

        val pager = Pager(users.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("users", users.content)

        modelAndView.model.put("viewCat", "admin_mgr")
        modelAndView.model.put("viewContent", "manager_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/manager/change_pass"), method = arrayOf(RequestMethod.GET))
    fun changePassword(): String = "/manager/change_pass :: content"

    @RequestMapping(path = arrayOf("/manager/change_pass"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun changePassword(@RequestParam(name = "newPass", required = true) newPassword: String): Boolean {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.principal is LoginManagerDetail) {
            val userDetail = auth.principal as LoginManagerDetail
            return managerService.resetPassword(userDetail.managerId, newPassword)
        } else {
            return false
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ManagerController::class.java)
    }
}
package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dao.WxMpAccountDao
import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.WeiXinService
import com.unicolour.joyspace.util.Pager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView

@Controller
class ManagerController {
    companion object {
        private val logger = LoggerFactory.getLogger(ManagerController::class.java)
    }

    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var weiXinService: WeiXinService

    @Autowired
    lateinit var wxMpAccountDao: WxMpAccountDao

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

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val users = if (name == null || name == "")
            managerDao.findByCompanyId(loginManager.companyId, pageable)
        else
            managerDao.findByCompanyIdAndUserNameOrFullName(loginManager.companyId, name, pageable)

        modelAndView.model.put("inputUserName", name)

        val pager = Pager(users.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("users", users.content)

        modelAndView.model.put("viewCat", "system_mgr")
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

    @GetMapping("/manager/wx/text_message")
    fun sendWxTextMessage(modelAndView: ModelAndView): ModelAndView {
        modelAndView.model["accounts"] = wxMpAccountDao.findAll()
        modelAndView.viewName = "/manager/wxTextMessage"

        return modelAndView
    }

    @PostMapping("/manager/wx/text_message")
    fun sendWxTextMessage(
            modelAndView: ModelAndView,
            @RequestParam(name = "wxMpAccountId", required = true) wxMpAccountId: Int,
            @RequestParam(name = "openIdList", required = true) openIdList: String,
            @RequestParam(name = "message", required = true) message: String,
            @RequestParam(name = "preview", required = true) preview: Boolean
    ): ModelAndView {

        if (managerService.loginManagerHasRole("ROLE_SUPERADMIN")) {
            weiXinService.sendTextMessage(
                    message,
                    openIdList.lines(),
                    wxMpAccountId,
                    preview)
        }

        modelAndView.viewName = "empty"
        return modelAndView
    }
}
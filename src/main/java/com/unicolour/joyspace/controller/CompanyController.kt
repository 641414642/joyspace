package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.service.CompanyService
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
class CompanyController {

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var companyService: CompanyService

    @RequestMapping("/company/list")
    fun companyList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20)
        val users = if (name == null || name == "")
            companyDao.findAll(pageable)
        else
            companyDao.findByName(name, pageable)

        modelAndView.model.put("inputCompanyName", name)

        val pager = Pager(users.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("users", users.content)

        modelAndView.model.put("viewCat", "user_mgr")
        modelAndView.model.put("viewContent", "company_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/company/edit"), method = arrayOf(RequestMethod.GET))
    fun editCompany(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var company: Company? = null
        if (id > 0) {
            company = companyDao.findOne(id)
        }

        if (company == null) {
            company = Company()
        }

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("company", company)
        modelAndView.viewName = "/company/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/company/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editCompany(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String): Boolean {

        if (id <= 0) {
            val company = companyService.createCompany(name, null)
            return company != null
        } else {
            return companyService.updateCompany(id, name)
        }
    }
}
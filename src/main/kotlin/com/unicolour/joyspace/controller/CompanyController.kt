package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.WeiXinPayConfigDao
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.WxPayParams
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    lateinit var weiXinPayConfigDao: WeiXinPayConfigDao

    @Autowired
    lateinit var companyService: CompanyService

    @RequestMapping("/company/list")
    fun companyList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val companies = if (name == null || name == "")
            companyDao.findAll(pageable)
        else
            companyDao.findByName(name, pageable)

        modelAndView.model.put("inputCompanyName", name)

        val pager = Pager(companies.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("companies", companies.content)

        modelAndView.model.put("viewCat", "system_mgr")
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

        val wxPayConfigs = weiXinPayConfigDao.findAll()

        if (company == null) {
            company = Company()
        }

        modelAndView.model.put("wxPayConfigs", wxPayConfigs)
        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("company", company)
        if (id > 0) {
            modelAndView.viewName = "/company/edit :: content_edit"
        }
        else {
            modelAndView.viewName = "/company/edit :: content_create"
        }

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/company/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editCompany(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "username", required = true) username: String,
            @RequestParam(name = "fullname", required = true) fullname: String,
            @RequestParam(name = "phone", required = true) phone: String,
            @RequestParam(name = "email", required = true) email: String,
            @RequestParam(name = "password", required = true) password: String
    ): CommonRequestResult {

        try {
            if (id <= 0) {
                companyService.createCompany(name.trim(), null, username.trim(), fullname, phone, email, password)
            } else {
                companyService.updateCompany(id, name.trim())
            }
            return CommonRequestResult()
        }catch(e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val msg = if (id <= 0) "创建投放商失败" else "修改投放商失败"
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, msg)
        }
    }
}
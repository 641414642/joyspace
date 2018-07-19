package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.CompanyWxAccountDao
import com.unicolour.joyspace.dao.WxMpAccountDao
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.util.Pager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
    companion object {
        val logger = LoggerFactory.getLogger(CompanyController::class.java)
    }

    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var wxMpAccountDao: WxMpAccountDao

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var companyWxAccountDao: CompanyWxAccountDao

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var managerService: ManagerService

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

        modelAndView.model["inputCompanyName"] = name

        val pager = Pager(companies.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        modelAndView.model["companies"] = companies.content

        modelAndView.model["viewCat"] = "system_mgr"
        modelAndView.model["viewContent"] = "company_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/company/edit"), method = arrayOf(RequestMethod.GET))
    fun editCompany(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var company: Company? = null
        var manager: Manager? = null
        if (id > 0) {
            company = companyDao.findOne(id)
            manager = managerService.getCompanyManager(id)
        }

        if (company == null) {
            company = Company()
        }

        modelAndView.model["create"] = id <= 0
        modelAndView.model["company"] = company
        modelAndView.model["manager"] = manager
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
            @RequestParam(name = "managerId", required = false, defaultValue = "0") managerId: Int,
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
                companyService.updateCompany(id, name.trim(), managerId, fullname, phone, email, password)
            }
            return CommonRequestResult()
        }catch(e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val msg = if (id <= 0) "创建投放商失败" else "修改投放商失败"
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, msg)
        }
    }

    @RequestMapping("/company/wxAccountList", method = arrayOf(RequestMethod.GET))
    fun companyWxAccountList(modelAndView: ModelAndView): ModelAndView {
        val loginManager = managerService.loginManager

        val accounts = companyWxAccountDao.getCompanyWxAccounts(loginManager!!.companyId)
        modelAndView.model["accounts"] = accounts

        modelAndView.model["viewCat"] = "system_mgr"
        modelAndView.model["viewContent"] = "company_wx_account"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping("/company/startAddWxAccount", method = arrayOf(RequestMethod.GET))
    fun startAddWxAccount(modelAndView: ModelAndView): ModelAndView {
        val activeWxMpAccount = wxMpAccountDao.findFirstByActiveIsTrue()
        if (activeWxMpAccount == null) {
            logger.warn("No active WxMpAccount, cannot start add wx account")
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val loginManager = managerService.loginManager
        val verifyCode = companyService.startAddCompanyWxAccount()

        if (companyWxAccountDao.countCompanyWxAccounts(loginManager!!.companyId) >= 10) {
            modelAndView.model["title"] = "提示"
            modelAndView.model["message"] = "最多只能添加10个微信收款账户"
            modelAndView.viewName = "/messageDialog :: content"
        }
        else {
            modelAndView.model["wxmpQrCode"] = activeWxMpAccount.qrCode
            modelAndView.model["qrcode"] = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                    "?appid=${activeWxMpAccount.appId}" +
                    "&redirect_uri=$baseUrl/company/wxAccountAddConfirm" +
                    "&response_type=code" +
                    "&scope=snsapi_userinfo"
            modelAndView.model["verifyCode"] = verifyCode

            modelAndView.viewName = "/company/wxAccountAdd :: content"
        }

        return modelAndView
    }

    @RequestMapping("/company/wxAccountAddConfirm", method = arrayOf(RequestMethod.GET))
    fun confirmAddWxAccount(
            modelAndView: ModelAndView,
            @RequestParam("code", required = true) code: String): ModelAndView
    {
        modelAndView.model["code"] = code

        modelAndView.viewName = "company/wxAccountAddConfirm"
        return modelAndView
    }

    @RequestMapping(path = arrayOf("/company/wxAccountAddConfirm"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun addWxAccount(
            @RequestParam(name = "code", required = true) code: String,
            @RequestParam(name = "realname", required = true) realname: String,
            @RequestParam(name = "phone", required = true) phoneNumber: String,
            @RequestParam(name = "verifyCode", required = true) verifyCode: String): CommonRequestResult
    {
        try {
            companyService.addCompanyWxAccount(code, realname, phoneNumber, verifyCode)
            return CommonRequestResult()
        }
        catch (e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        }
        catch (e: Exception) {
            e.printStackTrace()
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, "添加微信收款账号失败")
        }
    }

    @RequestMapping(path = arrayOf("/company/deleteWxAccount"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun deleteWxAccount(@RequestParam("accountId") accountId: Int): CommonRequestResult
    {
        try {
            companyService.deleteCompanyWxAccount(accountId)
            return CommonRequestResult()
        }
        catch (e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        }
        catch (e: Exception) {
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, "删除微信收款账号失败")
        }
    }

    @RequestMapping(path = arrayOf("/company/moveWxAccount"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun moveWxAccount(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "up", required = true) up: Boolean): Boolean {
        return companyService.moveCompanyWxAccount(id, up)
    }

    @RequestMapping(path = arrayOf("/company/toggleWxAccount"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun toggleWxAccount(@RequestParam(name = "id", required = true) id: Int): Boolean {
        return companyService.toggleCompanyWxAccount(id)
    }

}

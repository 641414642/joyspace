package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.util.Pager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.time.Duration
import java.time.Instant

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

    @Autowired
    lateinit var verifyCodeDao: VerifyCodeDao

    @Autowired
    lateinit var managerDao: ManagerDao

    @GetMapping("/company/list")
    fun companyList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "businessModel", required = false, defaultValue = "-1") businessModel: Int,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val businessModelEnum = BusinessModel.values().firstOrNull { it.value == businessModel }

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val companies = companyDao.queryCompanies(pageable, name ?: "", businessModelEnum)

        modelAndView.model["inputCompanyName"] = name
        modelAndView.model["inputBusinessModel"] = businessModel

        val pager = Pager(companies.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        modelAndView.model["businessModels"] = BusinessModel.values().filterNot { it == BusinessModel.DEFAULT }
        modelAndView.model["companies"] = companies.content

        modelAndView.model["viewCat"] = "system_mgr"
        modelAndView.model["viewContent"] = "company_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @GetMapping("/company/query")
    @ResponseBody
    fun companyQuery(
            @RequestParam(name = "name", required = false, defaultValue = "") name: String,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): Select2QueryResult {
        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val companies =
                if (name == "")
                    companyDao.findAll(pageable)
                else
                    companyDao.findByName(name, pageable)

        return Select2QueryResult(
                results = companies.content.map {
                    ResultItem(
                            id = it.id,
                            text = it.name
                    )
                },
                pagination = ResultPagination(more = companies.hasNext())
        )
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

        modelAndView.model["businessModels"] = BusinessModel.values().filterNot { it == BusinessModel.DEFAULT }
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
            @RequestParam(name = "businessModel", required = true) businessModel: Int,
            @RequestParam(name = "username", required = true) username: String,
            @RequestParam(name = "fullname", required = true) fullname: String,
            @RequestParam(name = "phone", required = true) phone: String,
            @RequestParam(name = "email", required = true) email: String,
            @RequestParam(name = "password", required = true) password: String
    ): CommonRequestResult {

        try {
            val businessModelEnum = BusinessModel.values().firstOrNull { it.value == businessModel } ?: BusinessModel.DEFAULT

            if (id <= 0) {
                companyService.createCompany(name.trim(), businessModelEnum, null, username.trim(), fullname, phone, email, password)
            } else {
                companyService.updateCompany(id, name.trim(), businessModelEnum, managerId, fullname, phone, email, password)
            }
            return CommonRequestResult()
        }catch(e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val msg = if (id <= 0) "创建投放商失败" else "修改投放商失败"
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, msg)
        }
    }


    @RequestMapping(path = arrayOf("/company/register"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun registerCompany(
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "username", required = true) username: String,
            @RequestParam(name = "fullname", required = true) fullname: String,
            @RequestParam(name = "phone", required = true) phone: String,
            @RequestParam(name = "email", required = true) email: String,
            @RequestParam(name = "password", required = true) password: String,
            @RequestParam(name = "verifyCode", required = true) verifyCode: String
    ): CommonRequestResult {

        return try {
            val now = Instant.now()
            val verifyCodeObj = verifyCodeDao.findOne(phone)
            if (verifyCodeObj == null ||
                    verifyCodeObj.code != verifyCode ||
                    Duration.between(verifyCodeObj.sendTime.toInstant(), now).seconds > 60 * 10) {  //超过10分钟
                throw ProcessException(ResultCode.INVALID_VERIFY_CODE)
            }
            companyService.createCompany(name.trim(), BusinessModel.DEFAULT, null, username.trim(), fullname, phone, email, password)
            verifyCodeDao.delete(verifyCodeObj)
            CommonRequestResult()
        } catch (e: ProcessException) {
            CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val msg = "注册失败"
            CommonRequestResult(ResultCode.OTHER_ERROR.value, msg)
        }
    }


    @RequestMapping(path = arrayOf("/company/sendVerifyCode"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun sendVerifyCode(
            @RequestParam(name = "phone", required = true) phone: String
    ): CommonRequestResult {

        return try {
            companyService.sendVerifyCode(phone)
            CommonRequestResult()
        } catch (e: ProcessException) {
            CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val msg = "发送验证码失败"
            CommonRequestResult(ResultCode.OTHER_ERROR.value, msg)
        }
    }


    @RequestMapping(path = arrayOf("/company/resetPassword"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun resetPassword(
            @RequestParam(name = "username", required = true) username: String,
            @RequestParam(name = "phone", required = true) phone: String,
            @RequestParam(name = "password", required = true) password: String,
            @RequestParam(name = "verifyCode", required = true) verifyCode: String
    ): CommonRequestResult {

        return try {
            val now = Instant.now()
            val verifyCodeObj = verifyCodeDao.findOne(phone)
            if (verifyCodeObj == null ||
                    verifyCodeObj.code != verifyCode ||
                    Duration.between(verifyCodeObj.sendTime.toInstant(), now).seconds > 60 * 10) {  //超过10分钟
                throw ProcessException(ResultCode.INVALID_VERIFY_CODE)
            }
            val manager = managerDao.findByUserName(username)
            if (manager == null || !manager.isEnabled) throw ProcessException(ResultCode.USER_NOT_FOUND)
            if (verifyCodeObj.phoneNumber != manager.cellPhone && verifyCodeObj.phoneNumber != manager.phone) throw ProcessException(ResultCode.USER_NOT_FOUND_FOR_THIS_PHONE_NUMBER)
            managerService.resetPassword(manager.id, password)
            CommonRequestResult()
        } catch (e: ProcessException) {
            CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val msg = "重置密码失败"
            CommonRequestResult(ResultCode.OTHER_ERROR.value, msg)
        }
    }






    @RequestMapping("/company/wxAccountList", method = arrayOf(RequestMethod.GET))
    fun companyWxAccountList(modelAndView: ModelAndView): ModelAndView {
        val loginManager = managerService.loginManager

        class WxAccountWrapper(val account: CompanyWxAccount, val wxMpAccount: WxMpAccount?)

        val accounts = companyWxAccountDao.getCompanyWxAccounts(loginManager!!.companyId).map {
            WxAccountWrapper(
                    account = it,
                    wxMpAccount = wxMpAccountDao.findOne(it.wxMpAccountId)
            )
        }
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

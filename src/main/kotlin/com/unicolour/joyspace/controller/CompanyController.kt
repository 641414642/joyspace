package com.unicolour.joyspace.controller

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.controller.api.WeixinNotifyController
import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.WeiXinPayConfigDao
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.QQMapGeoDecodeResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.WxPayParams
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.impl.PositionServiceImpl
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.ModelAndView

@Controller
class CompanyController {

    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var weiXinPayConfigDao: WeiXinPayConfigDao

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

    @RequestMapping("/company/wxAccountList", method = arrayOf(RequestMethod.GET))
    fun companyWxAccountList(modelAndView: ModelAndView): ModelAndView {
        modelAndView.model["viewCat"] = "system_mgr"
        modelAndView.model["viewContent"] = "company_wx_account"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping("/company/startAddWxAccount", method = arrayOf(RequestMethod.GET))
    fun startAddWxAccount(modelAndView: ModelAndView): ModelAndView {
        val verifyCode = companyService.startAddCompanyWxAccount()

        modelAndView.model["qrcode"] = "https://open.weixin.qq.com/connect/oauth2/authorize" +
            "?appid=wxffad6438f08103cb" +
            "&redirect_uri=$baseUrl/company/wxAccountAddConfirm" +
            "&response_type=code" +
            "&scope=snsapi_userinfo"
        modelAndView.model["verifyCode"] = verifyCode

        modelAndView.viewName = "/company/wxAccountAdd :: content"

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
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, "添加微信收款账号失败")
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class GetAccessTokenResult(
        var errcode: Int = 0,
        var errmsg: String = "",

        var access_token: String = "",
        var expires_in: Int = 0,
        var refresh_token: String = "",
        var openid: String = "",
        var scope: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
class GetUserInfoResult(
        var errcode: Int = 0,
        var errmsg: String = "",

        var openid: String = "",   //用户的唯一标识
        var nickname:String = "",   //用户昵称
        var headimgurl: String = ""  //用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
        //sex  用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
        //province  用户个人资料填写的省份

        //city  普通用户个人资料填写的城市
        //country   国家，如中国为CN
        //privilege  用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
        //unionid  只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。详见：获取用户个人信息（UnionID机制）
)


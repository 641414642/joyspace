package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.AliPayConfigDao
import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dao.WeiXinPayConfigDao
import com.unicolour.joyspace.dto.LoginManagerDetail
import com.unicolour.joyspace.model.AliPayConfig
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.model.WeiXinPayConfig
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
class PaymentController {

    @Autowired
    lateinit var weiXinPayConfigDao: WeiXinPayConfigDao

    @Autowired
    lateinit var aliPayConfigDao: AliPayConfigDao

    @RequestMapping("/wx_pay/list")
    fun wxPayList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20)
        val wxPayCfgs = if (name == null || name == "")
            weiXinPayConfigDao.findAll(pageable)
        else
            weiXinPayConfigDao.findByName(name, pageable)

        modelAndView.model.put("inputWxPayName", name)

        val pager = Pager(wxPayCfgs.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("wxpay_configs", wxPayCfgs.content)

        modelAndView.model.put("viewCat", "pay_mgr")
        modelAndView.model.put("viewContent", "wxpay_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/wx_pay/edit"), method = arrayOf(RequestMethod.GET))
    fun editWeiXinPay(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var wxPay: WeiXinPayConfig? = null
        if (id > 0) {
            wxPay = weiXinPayConfigDao.findOne(id)
        }

        if (wxPay == null) {
            wxPay = WeiXinPayConfig()
        }

        modelAndView.model.put("wxPay", wxPay)
        modelAndView.viewName = "/pay/wxpay_edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/wx_pay/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editWeiXinPay(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "appId", required = true) appId: String,
            @RequestParam(name = "mchId", required = true) mchId: String,
            @RequestParam(name = "keyVal", required = true) keyVal: String,
            @RequestParam(name = "appSecret", required = true) appSecret: String,
            @RequestParam(name = "enabled", required = false, defaultValue = "false") enabled: Boolean): Boolean {
        var wxPay: WeiXinPayConfig? = null
        if (id > 0) {
            wxPay = weiXinPayConfigDao.findOne(id)
        }

        if (wxPay == null) {
            wxPay = WeiXinPayConfig()
            wxPay.name = name
        }

        wxPay.appId = appId
        wxPay.mchId = mchId
        wxPay.keyVal = keyVal
        wxPay.appSecret = appSecret
        wxPay.enabled = enabled

        weiXinPayConfigDao.save(wxPay)
        return true
    }

    @RequestMapping("/ali_pay/list")
    fun aliPayList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20)
        val aliPayCfgs = if (name == null || name == "")
            aliPayConfigDao.findAll(pageable)
        else
            aliPayConfigDao.findByName(name, pageable)

        modelAndView.model.put("inputAliPayName", name)

        val pager = Pager(aliPayCfgs.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("alipay_configs", aliPayCfgs.content)

        modelAndView.model.put("viewCat", "pay_mgr")
        modelAndView.model.put("viewContent", "alipay_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/ali_pay/edit"), method = arrayOf(RequestMethod.GET))
    fun editAliPay(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var aliPay: AliPayConfig? = null
        if (id > 0) {
            aliPay = aliPayConfigDao.findOne(id)
        }

        if (aliPay == null) {
            aliPay = AliPayConfig()
        }

        modelAndView.model.put("aliPay", aliPay)
        modelAndView.viewName = "/pay/alipay_edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/ali_pay/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editAliPay(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "partner", required = true) partner: String,
            @RequestParam(name = "sellerEmail", required = true) sellerEmail: String,
            @RequestParam(name = "keyVal", required = true) keyVal: String,
            @RequestParam(name = "inputCharset", required = true) inputCharset: String,
            @RequestParam(name = "signType", required = true) signType: String,
            @RequestParam(name = "enabled", required = false, defaultValue = "false") enabled: Boolean): Boolean {
        var aliPay: AliPayConfig? = null
        if (id > 0) {
            aliPay = aliPayConfigDao.findOne(id)
        }

        if (aliPay == null) {
            aliPay = AliPayConfig()
            aliPay.name = name
        }

        aliPay.partner = partner
        aliPay.sellerEmail = sellerEmail
        aliPay.keyVal = keyVal
        aliPay.inputCharset = inputCharset
        aliPay.signType = signType
        aliPay.enabled = enabled

        aliPayConfigDao.save(aliPay)
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PaymentController::class.java)
    }
}
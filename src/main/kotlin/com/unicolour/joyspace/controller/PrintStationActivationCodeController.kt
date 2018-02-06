package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PrintStationActivationCodeService
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
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part
import kotlin.collections.ArrayList

@Controller
class PrintStationActivationCodeController {
    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var printStationActivationCodeDao: PrintStationActivationCodeDao

    @Autowired
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var printStationActivationCodeService: PrintStationActivationCodeService

    @RequestMapping("/activationCode/list")
    fun activationCodeist(modelAndView: ModelAndView,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int,
            @RequestParam(name = "statusInput", required = false, defaultValue = "-1") statusInput: Int
    ): ModelAndView {

        //val loginManager = managerService.loginManager

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val codes =
                if (statusInput == -1) {
                    printStationActivationCodeDao.findAll(pageable)
                } else if (statusInput == 0) {
                    printStationActivationCodeDao.findByUsedIsTrue(pageable)
                } else {
                    printStationActivationCodeDao.findByUsedIsFalse(pageable)
                }

        val pager = Pager(codes.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        modelAndView.model["statusInput"] = statusInput
        modelAndView.model["codes"] = codes

        modelAndView.model["viewCat"] = "system_mgr"
        modelAndView.model["viewContent"] = "activation_code_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/activationCode/create"), method = arrayOf(RequestMethod.GET))
    fun createActivationCode(modelAndView: ModelAndView): ModelAndView {
        val adSets = adSetDao.findByCompanyId(0)  //公用广告
        modelAndView.model["adSets"] = adSets
        modelAndView.model["defAdSetId"] = adSets.firstOrNull()?.id ?: 0

        modelAndView.viewName = "/activationCode/create :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/activationCode/create"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun activationCode(
            request: HttpServletRequest,
            @RequestParam(name = "printStationIdStart", required = true) printStationIdStart: Int,
            @RequestParam(name = "quantity", required = true) quantity: Int,
            @RequestParam(name = "printerType", required = true) printerType: String,
            @RequestParam(name = "proportion", required = true) proportion: Int,
            @RequestParam(name = "adSetId", required = true) adSetId: Int
    ): CommonRequestResult {
        try {
            val success = printStationActivationCodeService.batchCreateActivationCodes(
                    printStationIdStart,
                    quantity,
                    printerType,
                    proportion * 10,
                    adSetId)
            if (!success) {
                throw ProcessException(ResultCode.OTHER_ERROR, "创建自助机激活码失败")
            }
            else {
                return CommonRequestResult(0, null)
            }
        } catch (e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, "创建自助机激活码失败")
        }
    }
}


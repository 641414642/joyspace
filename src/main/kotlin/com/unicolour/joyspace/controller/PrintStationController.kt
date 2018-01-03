package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.ProductItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.PrintStationLoginSession
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.util.Pager
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
class PrintStationController {

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationService: PrintStationService

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

    @RequestMapping("/printStation/list")
    fun printStationList(
            modelAndView: ModelAndView,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val printStations = printStationDao.findByCompanyId(loginManager.companyId, pageable)

        val pager = Pager(printStations.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        class PrintStationInfo(val printStation: PrintStation, val online: Boolean, val version: String)

        val time = Calendar.getInstance()
        time.add(Calendar.SECOND, 3600 - 30)

        modelAndView.model.put("printStations",
                printStations.content.map {
                    val session = printStationLoginSessionDao.findByPrintStationId(it.id)
                    var online = false
                    var version = ""

                    if (session != null && session.expireTime.timeInMillis > time.timeInMillis) {    //自助机30秒之内访问过后台
                        online = true
                        version = if (session.version <= 0) "" else session.version.toString()
                    }

                    PrintStationInfo(it, online, version)
                })

        modelAndView.model.put("viewCat", "business_mgr")
        modelAndView.model.put("viewContent", "printStation_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printStation/edit"), method = arrayOf(RequestMethod.GET))
    fun editPrintStation(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int
    ): ModelAndView {
        val loginManager = managerService.loginManager

        var supportedProductIdSet: Set<Int> = emptySet<Int>()

        var printStation: PrintStation? = null
        if (id > 0) {
            printStation = printStationDao.findOne(id)
            supportedProductIdSet = printStationProductDao.findByPrintStationId(id).map { it.productId }.toHashSet()
        }

        if (printStation == null) {
            printStation = PrintStation()
        }

        val allProducts = productDao.findByCompanyIdOrderBySequenceAsc(loginManager!!.companyId)
                .sortedBy { it.sequence }
                .map {
                    ProductItem(
                            productId = it.id,
                            productType = it.template.type,
                            productName = it.name,
                            templateName = it.template.name,
                            selected = supportedProductIdSet.contains(it.id))
                }

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("printStation", printStation)
        modelAndView.model.put("positions", positionDao.findByCompanyId(loginManager!!.companyId))
        modelAndView.model.put("photo_products", allProducts.filter { it.productType == ProductType.PHOTO.value })
        modelAndView.model.put("template_products", allProducts.filter { it.productType == ProductType.TEMPLATE.value })
        modelAndView.model.put("id_photo_products", allProducts.filter { it.productType == ProductType.ID_PHOTO.value })
        modelAndView.model.put("productIds", allProducts.map { it.productId }.joinToString(separator = ","))
        modelAndView.viewName = "/printStation/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printStation/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editPrintStation(
            request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "password", required = true) password: String,
            @RequestParam(name = "positionId", required = true) positionId: Int,
            @RequestParam(name = "productIds", required = true) productIds: String
    ): Boolean {

        val selectedProductIds = productIds
                .split(',')
                .filter { !request.getParameter("product_${it}").isNullOrBlank() }
                .map { it.toInt() }
                .toSet()
        val baseUrl = getBaseUrl(request)

        if (id <= 0) {
            printStationService.createPrintStation(baseUrl, password, positionId, selectedProductIds)
            return true
        } else {
            return printStationService.updatePrintStation(id, baseUrl, password, positionId, selectedProductIds)
        }
    }

    @RequestMapping("/printStation/{id}")
    fun printStation(
            modelAndView: ModelAndView,
            @PathVariable("id") id: Int): ModelAndView {

        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            modelAndView.viewName = "/printStation/index"
        }
        else {
            modelAndView.viewName = "/printStation/notFound"
        }

        return modelAndView
    }
}

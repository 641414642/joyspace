package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dto.ProductItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.websocket.server.PathParam

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

        modelAndView.model.put("printStations", printStations.content)

        modelAndView.model.put("viewCat", "product_mgr")
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

        val allProducts = productDao.findByCompanyId(loginManager!!.companyId)
                .map { ProductItem(it.id, it.name, it.template.name, supportedProductIdSet.contains(it.id)) }

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("printStation", printStation)
        modelAndView.model.put("positions", positionDao.findAll())
        modelAndView.model.put("products", allProducts)
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
            @RequestParam(name = "wxQrCode", required = true) wxQrCode: String,
            @RequestParam(name = "positionId", required = true) positionId: Int,
            @RequestParam(name = "productIds", required = true) productIds: String
    ): Boolean {

        val selectedProductIds = productIds
                .split(',')
                .filter { !request.getParameter("product_${it}").isNullOrBlank() }
                .map { it.toInt() }
                .toSet()

        if (id <= 0) {
            printStationService.createPrintStation(password, wxQrCode, positionId, selectedProductIds)
            return true
        } else {
            return printStationService.updatePrintStation(id, password, wxQrCode, positionId, selectedProductIds)
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

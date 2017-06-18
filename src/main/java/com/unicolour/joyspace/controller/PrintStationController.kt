package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
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

    @RequestMapping("/printStation/list")
    fun printStationList(
            modelAndView: ModelAndView,
            @RequestParam(name = "sn", required = false, defaultValue = "") sn: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20)
        val printStations = if (sn == null || sn == "")
            printStationDao.findAll(pageable)
        else
            printStationDao.findBySn(sn, pageable)

        modelAndView.model.put("inputPrintStationSn", sn)

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

        var supportedProductIdSet: Set<Int> = emptySet<Int>()

        var printStation: PrintStation? = null
        if (id > 0) {
            printStation = printStationDao.findOne(id)
            supportedProductIdSet = printStationProductDao.findByPrintStationId(id).map { it.productId }.toHashSet()
        }

        if (printStation == null) {
            printStation = PrintStation()
        }

        val allProducts = productDao.findAll().map { ProductItem(it.id, it.name, supportedProductIdSet.contains(it.id)) }

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
            @RequestParam(name = "sn", required = true) sn: String,
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
            printStationService.createPrintStation(sn, wxQrCode, positionId, selectedProductIds)
            return true
        } else {
            return printStationService.updatePrintStation(id, sn, wxQrCode, positionId, selectedProductIds)
        }
    }
}

data class ProductItem(
        val productId: Int,
        val productName: String,
        val selected: Boolean
)
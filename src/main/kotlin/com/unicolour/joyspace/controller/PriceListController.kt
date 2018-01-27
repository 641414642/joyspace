package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PriceListDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PriceListService
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
import javax.servlet.http.HttpServletRequest

@Controller
class PriceListController {
    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var priceListDao: PriceListDao

    @Autowired
    lateinit var priceListService: PriceListService

    @Autowired
    lateinit var productDao: ProductDao

    @RequestMapping("/priceList/list")
    fun priceListList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val priceLists =
                if (name == null || name == "")
                    priceListDao.findByCompanyId(loginManager.companyId, pageable)
                else
                    priceListDao.findByCompanyIdAndName(loginManager.companyId, name, pageable)

        modelAndView.model.put("inputPriceListName", name)

        val pager = Pager(priceLists.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("priceLists", priceLists.content)

        modelAndView.model.put("viewCat", "product_mgr")
        modelAndView.model.put("viewContent", "priceList_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/priceList/edit"), method = arrayOf(RequestMethod.GET))
    fun editPriceList(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "mode", required = true) mode: String
    ): ModelAndView {

        var priceMap: Map<Int, Int> = emptyMap()
        var priceList: PriceList? = null
        if (id > 0) {
            priceList = priceListDao.findOne(id)
            priceMap = priceList.priceListItems.associateBy({it.productId}, {it.price})
        }

        if (priceList == null) {
            priceList = PriceList()
        }

        val allProducts = productDao.findAll().toList()
        val rows = allProducts.map {
            PriceRow(it.id, it.name,
                    String.format("%.2f", it.defaultPrice / 100.0),
                    if (priceMap.containsKey(it.id))
                        String.format("%.2f", priceMap[it.id]!! / 100.0)
                    else
                        ""
            )
        }

        modelAndView.model.put("mode", mode)
        modelAndView.model.put("modeDisplay", when(mode) {
            "edit" -> "编辑"
            "create" -> "新建"
            else -> "查看 - " + priceList.name
        })

        modelAndView.model.put("priceList", priceList)
        modelAndView.model.put("productIds", allProducts.map { it.id }.joinToString(separator = ","))
        modelAndView.model.put("rows", rows)
        modelAndView.viewName = "/priceList/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/priceList/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editPriceList(
            request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "productIds", required = true) productIds: String
    ): Boolean {

        val productIdPriceMap = productIds.split(',')
                .associateBy({ it.toInt() }, { request.getParameter("product_${it}") })

        if (id <= 0) {
            priceListService.createPriceList(name, productIdPriceMap)
            return true
        } else {
            return priceListService.updatePriceList(id, name, productIdPriceMap)
        }
    }
}

data class PriceRow(
        val productId: Int,
        val productName: String,
        val defPrice: String,
        val listPrice: String
)
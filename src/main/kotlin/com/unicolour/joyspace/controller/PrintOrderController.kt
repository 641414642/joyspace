package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PrintOrderDao
import com.unicolour.joyspace.dao.PrintOrderItemDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.util.Pager
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@Controller
class PrintOrderController {

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Autowired
    lateinit var printOrderItemDao: PrintOrderItemDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var managerService: ManagerService

    @RequestMapping("/printOrder/list")
    fun printOrderList(
            modelAndView: ModelAndView,
            @RequestParam(name = "orderNo", required = false, defaultValue = "") orderNo: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.DESC, "id")
        val printOrders = if (orderNo == null || orderNo == "")
            printOrderDao.findByCompanyIdOrderByIdDesc(loginManager!!.companyId, pageable)
        else
            printOrderDao.findByOrderNoIgnoreCaseAndCompanyId(orderNo, loginManager!!.companyId, pageable)

        modelAndView.model.put("inputOrderNo", orderNo)

        val pager = Pager(printOrders.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("printOrders", printOrders.content)

        modelAndView.model.put("viewCat", "business_mgr")
        modelAndView.model.put("viewContent", "printOrder_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }


    @RequestMapping(path = arrayOf("/printOrder/detail"), method = arrayOf(RequestMethod.GET))
    fun printOrderDetail(request: HttpServletRequest, modelAndView: ModelAndView, @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val baseUrl = getBaseUrl(request)

        val printOrder = printOrderDao.findOne(id)
        val printOrderItems = printOrderItemDao.findByPrintOrderId(id)

        val idProductMap = productDao.findByIdIn(printOrderItems.map { it.productId }).map { Pair(it.id, it) }.toMap()

        modelAndView.model.put("printOrder", printOrder)
        modelAndView.model.put("baseUrl", baseUrl)
        modelAndView.model.put("orderItems", printOrderItems.map { Pair(it, idProductMap[it.productId]) })
        modelAndView.viewName = "/printOrder/detail :: content"

        return modelAndView
    }

}
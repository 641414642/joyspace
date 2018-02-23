package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.WxEntTransferRecord
import com.unicolour.joyspace.model.WxEntTransferRecordItem
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
class PrintOrderController {
    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var userDao: UserDao

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Autowired
    lateinit var printOrderItemDao: PrintOrderItemDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var wxEntTransferRecordDao: WxEntTransferRecordDao

    @Autowired
    lateinit var wxEntTransferRecordItemDao: WxEntTransferRecordItemDao

    @RequestMapping("/printOrder/list")
    fun printOrderList(
            modelAndView: ModelAndView,
            @RequestParam(name = "orderNo", required = false, defaultValue = "") orderNo: String?,
            @RequestParam(name = "autoRefresh", required = false, defaultValue = "false") autoRefresh: Boolean,
            @RequestParam(name = "partial", required = false, defaultValue = "false") partial: Boolean?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        val pageable = PageRequest(pageno - 1, 50, Sort.Direction.DESC, "id")
        val printOrders = if (orderNo == null || orderNo == "")
            printOrderDao.findByCompanyIdOrderByIdDesc(loginManager!!.companyId, pageable)
        else
            printOrderDao.findByOrderNoIgnoreCaseAndCompanyId(orderNo, loginManager!!.companyId, pageable)

        val idUserMap = userDao.findByIdIn(printOrders.content.map { it.userId }).map { Pair(it.id, it) }.toMap()
        val idPrintStationMap = printStationDao.findByIdIn(printOrders.content.map { it.printStationId }).map { Pair(it.id, it) }.toMap()
        val idPositionMap = positionDao.findByIdIn(idPrintStationMap.values.map { it.positionId }).map { Pair(it.id, it) }.toMap()

        modelAndView.model["inputOrderNo"] = orderNo

        val pager = Pager(printOrders.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        class PrintOrderWrapper(val order: PrintOrder, val position: Position, val userName: String)

        modelAndView.model["printOrders"] = printOrders.content.map {
            val printStation = idPrintStationMap[it.printStationId]!!
            val position = idPositionMap[printStation.positionId]!!
            val user = idUserMap[it.userId]!!
            var userName = user.nickName ?: user.fullName ?: ""
            if (userName != "") {
                userName = " / " + userName
            }
            PrintOrderWrapper(it, position, "ID:${user.id}" + userName)
        }

        modelAndView.model["autoRefresh"] = autoRefresh

        if (partial == true) {
            modelAndView.viewName = "/printOrder/list :: order_list_content"
        }
        else {
            modelAndView.model["viewCat"] = "business_mgr"
            modelAndView.model["viewContent"] = "printOrder_list"
            modelAndView.viewName = "layout"
        }

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printOrder/detail"), method = arrayOf(RequestMethod.GET))
    fun printOrderDetail(modelAndView: ModelAndView, @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val printOrder = printOrderDao.findOne(id)
        val printOrderItems = printOrderItemDao.findByPrintOrderId(id)

        val idProductMap = productDao.findByIdIn(printOrderItems.map { it.productId }).map { Pair(it.id, it) }.toMap()

        modelAndView.model.put("printOrder", printOrder)
        modelAndView.model.put("baseUrl", baseUrl)
        modelAndView.model.put("orderItems", printOrderItems.map { Pair(it, idProductMap[it.productId]) })
        modelAndView.viewName = "/printOrder/detail :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printOrder/transferDetail"), method = arrayOf(RequestMethod.GET))
    fun printOrderTransferDetail(modelAndView: ModelAndView, @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val loginManager = managerService.loginManager

        val printOrder = printOrderDao.findOne(id)
        val recordItem = wxEntTransferRecordItemDao.findByPrintOrderId(id)
        val record = wxEntTransferRecordDao.findOne(recordItem!!.recordId)

        if (loginManager!!.companyId != record.companyId) {
            throw ProcessException(ResultCode.OTHER_ERROR)
        }

        modelAndView.model["order"] = printOrder
        modelAndView.model["record"] = record
        modelAndView.model["recordItem"] = recordItem
        modelAndView.viewName = "/printOrder/transferDetail :: content"

        return modelAndView
    }

}
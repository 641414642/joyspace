package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.BusinessModel
import com.unicolour.joyspace.model.StationType
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PrintOrderService
import com.unicolour.joyspace.util.Pager
import com.unicolour.joyspace.view.PrintOrderExcelView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@Controller
class PrintOrderController {
    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var companyDao: CompanyDao

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
    lateinit var printOrderService: PrintOrderService

    @Autowired
    lateinit var wxEntTransferRecordDao: WxEntTransferRecordDao

    @Autowired
    lateinit var wxEntTransferRecordItemDao: WxEntTransferRecordItemDao

    private fun parseDate(dateStr: String): Calendar {
        val cal = Calendar.getInstance()

        if (dateStr.isEmpty()) {
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        else {
            cal.timeInMillis = SimpleDateFormat("yyyy-MM-dd").parse(dateStr).time
        }

        return cal
    }

    @GetMapping("/printOrder/export")
    fun printOrderExport(
            modelAndView: ModelAndView,
            @RequestParam(name = "companyId", required = false, defaultValue = "0") inputCompanyId: Int,
            @RequestParam(name = "positionId", required = false, defaultValue = "0") positionId: Int,
            @RequestParam(name = "printStationId", required = false, defaultValue = "0") printStationId: Int,
            @RequestParam(name = "startTime", required = false, defaultValue = "") startTime: String,
            @RequestParam(name = "endTime", required = false, defaultValue = "") endTime: String): ModelAndView {
        val isSuperAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")

        val companyId = if (isSuperAdmin) {
            inputCompanyId
        } else {
            managerService.loginManager!!.companyId
        }

        val startTimeObj = parseDate(startTime)
        val endTimeObj = parseDate(endTime)

        val endTime1 = (endTimeObj.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val printOrders = printOrderService.queryPrinterOrders(
                companyId, startTimeObj, endTime1, positionId, printStationId, "id desc")

        val orderStat = printOrderService.printOrderStat(companyId, startTimeObj, endTime1, positionId, printStationId)

        modelAndView.model["photoCopies"] = orderStat.printPageCount
        modelAndView.model["turnOver"] = orderStat.totalAmount - orderStat.totalDiscount
        modelAndView.model["printOrderCount"] = printOrders.size
        modelAndView.model["printOrders"] = printOrders
        modelAndView.model["companyIdBusinessModelMap"] =   //Map<Int, Int>
                if (isSuperAdmin) {
                    printOrders.asSequence()
                            .map { it.companyId }
                            .distinct()
                            .map { it to (companyDao.findOne(it)?.businessModel ?: BusinessModel.DEFAULT.value) }
                            .toMap()
                }
                else {
                    emptyMap()
                }

        modelAndView.model["printStationIdStationTypeMap"] =     //Map<Int, Int>
                if (isSuperAdmin) {
                    printOrders.asSequence()
                            .map { it.printStationId }
                            .distinct()
                            .map { it to (printStationDao.findOne(it)?.stationType ?: StationType.DEFAULT.value) }
                            .toMap()
                }
                else {
                    emptyMap()
                }

        modelAndView.view = PrintOrderExcelView(isSuperAdmin)

        return modelAndView
    }

    @GetMapping("/printOrder/list")
    fun printOrderList(
            modelAndView: ModelAndView,
            @RequestParam(name = "query", required = false, defaultValue = "false") query: Boolean,
            @RequestParam(name = "inputCompanyId", required = false, defaultValue = "0") inputCompanyId: Int,
            @RequestParam(name = "inputPositionId", required = false, defaultValue = "0") inputPositionId: Int,
            @RequestParam(name = "inputPrintStationId", required = false, defaultValue = "0") inputPrintStationId: Int,
            @RequestParam(name = "inputTimeRange", required = false, defaultValue = "1") inputTimeRange: Int,
            @RequestParam(name = "inputStartTime", required = false, defaultValue = "") inputStartTime: String,
            @RequestParam(name = "inputEndTime", required = false, defaultValue = "") inputEndTime: String,
            @RequestParam(name = "partial", required = false, defaultValue = "false") partial: Boolean?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager!!

        val isSuperAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")
        val isEpson = managerService.loginManagerHasRole("ROLE_EPSON")

        val companyId = if (isSuperAdmin || isEpson) {
                inputCompanyId
            } else {
                loginManager.companyId
            }

        val startTime = parseDate(inputStartTime)
        val endTime = parseDate(inputEndTime)

        val endTime1 = (endTime.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val printOrders = printOrderService.queryPrinterOrders(pageno, 50,
                companyId, startTime, endTime1, inputPositionId, inputPrintStationId, "id desc")

        val pager = Pager(printOrders.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager
        modelAndView.model["printOrders"] = printOrders.content

        val orderStat = printOrderService.printOrderStat(companyId, startTime, endTime1, inputPositionId, inputPrintStationId)
        val turnOver = orderStat.totalAmount - orderStat.totalDiscount

        modelAndView.model["orderCount"] = orderStat.orderCount
        modelAndView.model["photoCopies"] = orderStat.printPageCount
        modelAndView.model["turnOver"] = "${turnOver/100}.${String.format("%02d", turnOver%100)}"

        modelAndView.model["query"] = query

        if (partial == true) {
            modelAndView.viewName = "/printOrder/list :: order_list_content"
        }
        else {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")

            if (isSuperAdmin || isEpson) {
                modelAndView.model["company"] = if (inputCompanyId > 0) companyDao.findOne(inputCompanyId) else null
            }

            modelAndView.model["loginCompanyId"] = loginManager.companyId
            modelAndView.model["position"] = if (inputPositionId > 0) positionDao.findOne(inputPositionId) else null
            modelAndView.model["printStation"] = if (inputPrintStationId > 0) printStationDao.findOne(inputPrintStationId) else null

            modelAndView.model["inputTimeRange"] = inputTimeRange
            modelAndView.model["inputStartTime"] = dateFormat.format(startTime.timeInMillis)
            modelAndView.model["inputEndTime"] = dateFormat.format(endTime.timeInMillis)

            val startOfToday = Calendar.getInstance().apply { toStartOfTheDay(this) }
            val startOfTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1); toStartOfTheDay(this) }
            val startOfTwoDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2); toStartOfTheDay(this) }

            val startOfThisMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                toStartOfTheDay(this)
            }

            val startOfNextMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                toStartOfTheDay(this)
                add(Calendar.MONTH, 1)
            }

            val todayStat = printOrderService.printOrderStat(companyId, startOfToday, startOfTomorrow, inputPositionId, inputPrintStationId)
            val lastTwoDaysStat = printOrderService.printOrderStat(companyId, startOfTwoDaysAgo, startOfToday, inputPositionId, inputPrintStationId)
            val monthStat = printOrderService.printOrderStat(companyId, startOfThisMonth, startOfNextMonth, inputPositionId, inputPrintStationId)

            modelAndView.model["orderCount_today"] = todayStat.orderCount
            modelAndView.model["printPageCount_today"] = todayStat.printPageCount
            modelAndView.model["income_today"] = todayStat.totalAmount - todayStat.totalDiscount

            modelAndView.model["orderCount_lastThreeDays"] = todayStat.orderCount + lastTwoDaysStat.orderCount
            modelAndView.model["printPageCount_lastThreeDays"] = todayStat.printPageCount + lastTwoDaysStat.printPageCount
            modelAndView.model["income_lastThreeDays"] = todayStat.totalAmount - todayStat.totalDiscount + lastTwoDaysStat.totalAmount - lastTwoDaysStat.totalDiscount

            modelAndView.model["orderCount_month"] = monthStat.orderCount
            modelAndView.model["printPageCount_month"] = monthStat.printPageCount
            modelAndView.model["income_month"] = monthStat.totalAmount - monthStat.totalDiscount

            modelAndView.model["viewCat"] = "business_mgr"
            modelAndView.model["viewContent"] = "printOrder_list"
            modelAndView.viewName = "layout"
        }

        return modelAndView
    }

    @RequestMapping("/printOrder/stat")
    fun printOrderStat(
            modelAndView: ModelAndView,
            @RequestParam(name = "inputPositionId", required = false, defaultValue = "0") inputPositionId: Int,
            @RequestParam(name = "inputPrintStationId", required = false, defaultValue = "0") inputPrintStationId: Int): ModelAndView {

        val loginManager = managerService.loginManager
        val companyId = loginManager!!.companyId

        val startOfToday = Calendar.getInstance().apply { toStartOfTheDay(this) }
        val startOfTomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1); toStartOfTheDay(this) }
        val startOfTwoDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2); toStartOfTheDay(this) }

        val startOfThisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            toStartOfTheDay(this)
        }

        val startOfNextMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            toStartOfTheDay(this)
            add(Calendar.MONTH, 1)
        }

        val todayStat = printOrderService.printOrderStat(companyId, startOfToday, startOfTomorrow, inputPositionId, inputPrintStationId)
        val lastTwoDaysStat = printOrderService.printOrderStat(companyId, startOfTwoDaysAgo, startOfToday, inputPositionId, inputPrintStationId)
        val monthStat = printOrderService.printOrderStat(companyId, startOfThisMonth, startOfNextMonth, inputPositionId, inputPrintStationId)

        modelAndView.model["orderCount_today"] = todayStat.orderCount
        modelAndView.model["printPageCount_today"] = todayStat.printPageCount
        modelAndView.model["income_today"] = todayStat.totalAmount - todayStat.totalDiscount

        modelAndView.model["orderCount_lastThreeDays"] = todayStat.orderCount + lastTwoDaysStat.orderCount
        modelAndView.model["printPageCount_lastThreeDays"] = todayStat.printPageCount + lastTwoDaysStat.printPageCount
        modelAndView.model["income_lastThreeDays"] = todayStat.totalAmount - todayStat.totalDiscount + lastTwoDaysStat.totalAmount - lastTwoDaysStat.totalDiscount

        modelAndView.model["orderCount_month"] = monthStat.orderCount
        modelAndView.model["printPageCount_month"] = monthStat.printPageCount
        modelAndView.model["income_month"] = monthStat.totalAmount - monthStat.totalDiscount

        modelAndView.viewName = "/printOrder/list :: order_stat"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printOrder/detail"), method = arrayOf(RequestMethod.GET))
    fun printOrderDetail(modelAndView: ModelAndView, @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val printOrder = printOrderDao.findOne(id)
        val printOrderItems = printOrderItemDao.findByPrintOrderId(id)

        val idProductMap = productDao.findByIdIn(printOrderItems.map { it.productId }).map { Pair(it.id, it) }.toMap()

        modelAndView.model["printOrder"] = printOrder
        modelAndView.model["baseUrl"] = baseUrl
        modelAndView.model["orderItems"] = printOrderItems.map { Pair(it, idProductMap[it.productId]) }
        modelAndView.viewName = "/printOrder/detail :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printOrder/transferDetail"), method = arrayOf(RequestMethod.GET))
    fun printOrderTransferDetail(modelAndView: ModelAndView, @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val loginManager = managerService.loginManager

        val printOrder = printOrderDao.findOne(id)
        val recordItem = wxEntTransferRecordItemDao.findByPrintOrderId(id)
        val record = wxEntTransferRecordDao.findOne(recordItem!!.recordId)

        if (!managerService.loginManagerHasRole("ROLE_SUPERADMIN") && loginManager!!.companyId != record.companyId) {
            throw ProcessException(ResultCode.OTHER_ERROR)
        }

        modelAndView.model["order"] = printOrder
        modelAndView.model["record"] = record
        modelAndView.model["recordItem"] = recordItem
        modelAndView.viewName = "/printOrder/transferDetail :: content"

        return modelAndView
    }

    @GetMapping("/printOrder/reprint")
    fun reprintOrder(
            @RequestParam(name = "printOrderId", required = true) printOrderId: Int,
            modelAndView: ModelAndView
    ): ModelAndView {
        val printOrder = printOrderDao.findOne(printOrderId)
        val printStation = printStationDao.findOne(printOrder.printStationId)

        modelAndView.model["printOrderId"] = printOrderId
        modelAndView.model["printOrderNo"] = printOrder.orderNo
        modelAndView.model["curPrintStationId"] = printOrder.printStationId
        modelAndView.model["printStations"] = printStationDao.findByPositionId(printStation.positionId)

        modelAndView.viewName = "/printOrder/reprint :: content"
        return modelAndView
    }

    @PostMapping("/printOrder/reprint")
    @ResponseBody
    fun reprintOrder(
            @RequestParam(name = "printOrderId", required = true) printOrderId: Int,
            @RequestParam(name = "printStationId", required = true) printStationId: Int
    ): CommonRequestResult {
        try {
            printOrderService.addReprintOrderTask(printOrderId, printStationId)
            return CommonRequestResult()
        } catch (e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        }
    }

    private fun toStartOfTheDay(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
}
package com.unicolour.joyspace.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.PrintStationStatus
import com.unicolour.joyspace.model.PrintStationTaskType
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PrintOrderService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.util.Pager
import com.unicolour.joyspace.view.PrintStationExcelView
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import kotlin.collections.ArrayList
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.text.DecimalFormat


@Controller
class PrintStationController {
    companion object {
        val logger = LoggerFactory.getLogger(PrintStationController::class.java)
    }

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationService: PrintStationService

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var printOrderService: PrintOrderService

    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

    @Autowired
    lateinit var printerTypeDao: PrinterTypeDao

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String


    @GetMapping("/printStation/query")
    @ResponseBody
    fun printStationQuery(
            @RequestParam(name = "name", required = false, defaultValue = "") name: String,
            @RequestParam(name = "companyId", required = false, defaultValue = "0") companyId: Int,
            @RequestParam(name = "positionId", required = false, defaultValue = "0") positionId: Int,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): Select2QueryResult {

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val printStations = printStationDao.queryPrintStations(
                pageable = pageable,
                companyId = companyId,
                positionId = positionId,
                printStationId = 0,
                name = name,
                printerModel = "",
                onlineOnly = false
        )

        return Select2QueryResult(
                results = printStations.content.map {
                    ResultItem(
                            id = it.id,
                            text = if (it.name.isBlank()) "自助机${it.id}" else it.name
                    )
                },
                pagination = ResultPagination(more = printStations.hasNext())
        )
    }

    class PrintStationInfo(val printStation: PrintStation, val online: Boolean, val printerTypeDisp: String, val paperSizeDisp: String)

    @RequestMapping("/printStation/list")
    fun printStationList(
            modelAndView: ModelAndView,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int,
            @RequestParam(name = "inputPositionId", required = false, defaultValue = "0") inputPositionId: Int
    ): ModelAndView {

        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val printerNameDispMap = printerTypeDao.findAll().map { it.name to it.displayName }.toMap()

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val printStations = if (inputPositionId > 0)
                printStationDao.findByCompanyIdAndPositionId(loginManager.companyId, inputPositionId, pageable)
            else
                printStationDao.findByCompanyId(loginManager.companyId, pageable)

        val pager = Pager(printStations.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        val time = System.currentTimeMillis() - 30 * 1000

        modelAndView.model["positions"] = positionDao.findByCompanyId(loginManager.companyId)
        modelAndView.model["printStations"] = printStations.content.map {
            val lastAccessTime = it.lastAccessTime
            val online = lastAccessTime != null && lastAccessTime.timeInMillis > time   //自助机30秒之内访问过后台

            val printerTypeDisp = printerNameDispMap[it.printerType] ?: ""
            val paperSizeDisp = getPaperSizeDisplay(it)
            PrintStationInfo(it, online, printerTypeDisp, paperSizeDisp)
        }

        modelAndView.model["inputPositionId"] = inputPositionId
        modelAndView.model["viewCat"] = "business_mgr"
        modelAndView.model["viewContent"] = "printStation_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @GetMapping("/printStation/allList")
    fun allPrintStationList(
            modelAndView: ModelAndView,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int,
            @RequestParam(name = "inputCompanyId", required = false, defaultValue = "0") inputCompanyId: Int,
            @RequestParam(name = "inputPositionId", required = false, defaultValue = "0") inputPositionId: Int,
            @RequestParam(name = "inputPrintStationId", required = false, defaultValue = "0") inputPrintStationId: Int,
            @RequestParam(name = "inputName", required = false, defaultValue = "") inputName: String,
            @RequestParam(name = "inputPrinterModel", required = false, defaultValue = "") inputPrinterModel: String,
            @RequestParam(name = "inputOnlineOnly", required = false, defaultValue = "false") inputOnlineOnly: Boolean
    ): ModelAndView {

        val printerNameDispMap = printerTypeDao.findAll().map { it.name to it.displayName }.toMap()

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val printStations = printStationDao.queryPrintStations(
                pageable = pageable,
                companyId = inputCompanyId,
                positionId = inputPositionId,
                printStationId = inputPrintStationId,
                name = inputName,
                printerModel = inputPrinterModel,
                onlineOnly = inputOnlineOnly
        )

        val pager = Pager(printStations.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        val time = System.currentTimeMillis() - 30 * 1000

        modelAndView.model["printStations"] = printStations.content.map {
            val lastAccessTime = it.lastAccessTime
            val online = lastAccessTime != null && lastAccessTime.timeInMillis > time   //自助机30秒之内访问过后台

            val printerTypeDisp = printerNameDispMap[it.printerType] ?: ""
            val paperSizeDisp = getPaperSizeDisplay(it)
            PrintStationInfo(it, online, printerTypeDisp, paperSizeDisp)
        }
        modelAndView.model["printStationCount"] = printStations.totalElements

        modelAndView.model["company"] = if (inputCompanyId > 0) companyDao.findOne(inputCompanyId) else null
        modelAndView.model["position"] = if (inputPositionId > 0) positionDao.findOne(inputPositionId) else null
        modelAndView.model["inputName"] = inputName
        modelAndView.model["inputPrinterModel"] = inputPrinterModel
        modelAndView.model["inputPrintStationId"] = if (inputPrintStationId <= 0) "" else inputPrintStationId.toString()
        modelAndView.model["inputOnlineOnly"] = inputOnlineOnly

        modelAndView.model["viewCat"] = "system_mgr"
        modelAndView.model["viewContent"] = "printStation_allList"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @GetMapping("/printStation/export")
    fun exportPrintStationList(
            modelAndView: ModelAndView,
            @RequestParam(name = "inputCompanyId", required = false, defaultValue = "0") inputCompanyId: Int,
            @RequestParam(name = "inputPositionId", required = false, defaultValue = "0") inputPositionId: Int,
            @RequestParam(name = "inputPrintStationId", required = false, defaultValue = "0") inputPrintStationId: Int,
            @RequestParam(name = "inputName", required = false, defaultValue = "") inputName: String,
            @RequestParam(name = "inputPrinterModel", required = false, defaultValue = "") inputPrinterModel: String,
            @RequestParam(name = "inputOnlineOnly", required = false, defaultValue = "false") inputOnlineOnly: Boolean
    ): ModelAndView {

        val printerNameDispMap = printerTypeDao.findAll().map { it.name to it.displayName }.toMap()

        val printStations = printStationDao.queryPrintStations(
                companyId = inputCompanyId,
                positionId = inputPositionId,
                printStationId = inputPrintStationId,
                name = inputName,
                printerModel = inputPrinterModel,
                onlineOnly = inputOnlineOnly
        )

        val time = System.currentTimeMillis() - 30 * 1000

        modelAndView.model["printStations"] = printStations.sortedBy { it.id }.map {
            val lastAccessTime = it.lastAccessTime
            val online = lastAccessTime != null && lastAccessTime.timeInMillis > time   //自助机30秒之内访问过后台

            val printerTypeDisp = printerNameDispMap[it.printerType] ?: ""
            val paperSizeDisp = getPaperSizeDisplay(it)
            PrintStationInfo(it, online, printerTypeDisp, paperSizeDisp)
        }
        modelAndView.model["printStationCount"] = printStations.size

        modelAndView.view = PrintStationExcelView()

        return modelAndView
    }

    private fun getPaperSizeDisplay(printStation: PrintStation): String {
        return if (printStation.paperWidth == null || printStation.paperLength == null) {
            ""
        } else {
            val fmt = DecimalFormat("0.#")
            var widInInch = printStation.paperWidth!! / 25.4
            var heiInInch = printStation.paperLength!! / 25.4

            if (widInInch % 1.0 < 0.2) {
                widInInch = Math.floor(widInInch)
            }

            if (heiInInch % 1.0 < 0.2) {
                heiInInch = Math.floor(heiInInch)
            }

            fmt.format(widInInch) + " x " + fmt.format(heiInInch)
        }
    }

    @RequestMapping(path = arrayOf("/printStation/edit"), method = arrayOf(RequestMethod.GET))
    fun editPrintStation(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int
    ): ModelAndView {
        val printStation: PrintStation = printStationDao.findOne(id)
        val supportedProductIdSet = printStationProductDao.findByPrintStationId(id).map { it.productId }.toHashSet()
        val companyId = printStation.companyId

        val products = productService.queryProducts(printStation.companyId, "", true, "sequence asc")
                .map {
                    ProductItem(
                            productId = it.id,
                            productType = it.template.type,
                            productName = it.name,
                            templateName = it.template.name,
                            selected = supportedProductIdSet.contains(it.id))
                }

        modelAndView.model["printStation"] = printStation
        modelAndView.model["positions"] = positionDao.findByCompanyId(companyId)
        modelAndView.model["adSets"] = adSetDao.queryAdSets(companyId, "", true)
        modelAndView.model["photo_products"] = products.filter { it.productType == ProductType.PHOTO.value }
        modelAndView.model["template_products"] = products.filter { it.productType == ProductType.TEMPLATE.value }
        modelAndView.model["id_photo_products"] = products.filter { it.productType == ProductType.ID_PHOTO.value }
        modelAndView.model["album_products"] = products.filter { it.productType == ProductType.ALBUM.value }
        modelAndView.model["productIds"] = products.map { it.productId }.joinToString(separator = ",")
        modelAndView.model["printerTypes"] = printerTypeDao.findAll()

        modelAndView.viewName = "/printStation/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printStation/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editPrintStation(
            request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "printStationName", required = true) printStationName: String,
            @RequestParam(name = "positionId", required = true) positionId: Int,
            @RequestParam(name = "proportion", required = false, defaultValue = "0") proportion: Double,
            @RequestParam(name = "printerType", required = true, defaultValue = "") printerType: String,
            @RequestParam(name = "adSetId", required = false, defaultValue = "-1") adSetId: Int,
            @RequestParam(name = "productIds", required = true) productIds: String
    ): Boolean {

        val selectedProductIds = productIds
                .split(',')
                .filter { !request.getParameter("product_$it").isNullOrBlank() }
                .map { it.toInt() }
                .toSet()

        return printStationService.updatePrintStation(id, printStationName,
                positionId, (proportion * 10).toInt(), printerType, adSetId, selectedProductIds)
    }

    @RequestMapping(path = arrayOf("/printStation/activate"), method = arrayOf(RequestMethod.GET))
    fun activatePrintStation(
            @RequestParam(name = "allCompany", required = false, defaultValue = "false") allCompany: Boolean,
            modelAndView: ModelAndView): ModelAndView {
        val printStation = PrintStation()

        val products = productService.queryProducts(printStation.companyId, "", true, "sequence asc")
                .map {
                    ProductItem(
                            productId = it.id,
                            productType = it.template.type,
                            productName = it.name,
                            templateName = it.template.name,
                            selected = it.template.type == ProductType.ID_PHOTO.value ||
                                            (it.template.type == ProductType.PHOTO.value && it.name.contains("6寸")) ||
                                            (it.template.type == ProductType.PHOTO.value && it.name.contains("六寸"))
                    )
                }

        val superAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")

        if (superAdmin && allCompany) {
            modelAndView.model["companies"] = companyDao.findAll()
            modelAndView.model["positions"] = positionDao.findAll()
        }
        else {
            val loginManager = managerService.loginManager
            modelAndView.model["positions"] = positionDao.findByCompanyId(loginManager!!.companyId)
        }

        modelAndView.model["printStation"] = printStation
        modelAndView.model["allCompany"] = allCompany
        modelAndView.model["photo_products"] = products.filter { it.productType == ProductType.PHOTO.value }
        modelAndView.model["template_products"] = products.filter { it.productType == ProductType.TEMPLATE.value }
        modelAndView.model["album_products"] = products.filter { it.productType == ProductType.ALBUM.value }
        modelAndView.model["id_photo_products"] = products.filter { it.productType == ProductType.ID_PHOTO.value }
        modelAndView.model["productIds"] = products.map { it.productId }.joinToString(separator = ",")
        modelAndView.viewName = "/printStation/activate :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/printStation/activate"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun activatePrintStation(
            request: HttpServletRequest,
            @RequestParam(name = "code", required = true) code: String,
            @RequestParam(name = "printStationName", required = true) printStationName: String,
            @RequestParam(name = "positionId", required = true) positionId: Int,
            @RequestParam(name = "productIds", required = true) productIds: String
    ): CommonRequestResult {

        try {
            val selectedProductIds = productIds
                    .split(',')
                    .filter { !request.getParameter("product_${it}").isNullOrBlank() }
                    .map { it.toInt() }
                    .toSet()
            printStationService.activatePrintStation(null, code, printStationName, positionId, selectedProductIds, "")
            return CommonRequestResult()
        } catch (e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, "创建自助机失败")
        }
    }

    @RequestMapping("/printStation/{id}")
    fun printStation(
            modelAndView: ModelAndView,
            @PathVariable("id") id: String?): ModelAndView {

        if (id == "LLWDtNhzLW"){
            return ModelAndView("redirect:$baseUrl/LLWDtNhzLW.txt")
        }

        val printStation = printStationDao.findOne(id?.toInt())

        if (printStation != null) {
            modelAndView.viewName = "/printStation/index"
        }
        else {
            modelAndView.viewName = "/printStation/notFound"
        }

        return modelAndView
    }


    @GetMapping("/printStation/qrCode/{id}")
    fun printStationQrCode(
            @PathVariable("id") id: Int,
            @RequestParam("noBackground", required = false, defaultValue = "false") noBackground: Boolean,
            attributes: RedirectAttributes
    ): RedirectView {
        val qrCodeImgUrl = printStationService.getPrintStationQrCodeUrl(id, noBackground)
        attributes.addFlashAttribute("flashAttribute", "redirectWithRedirectView")
        return RedirectView(qrCodeImgUrl)
    }

    @RequestMapping("/printStation/tasks", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun fetchedPrintStationTasks(
            @RequestParam("sessionId") sessionId: String,
            @RequestParam("taskIdAfter") taskIdAfter: Int) : List<PrintStationTaskDTO> {
        val tasks = printStationService.getUnFetchedPrintStationTasks(sessionId, taskIdAfter)
        val taskDTOs = ArrayList<PrintStationTaskDTO>()

        for (task in tasks) {
            var param = task.param
            if (task.type == PrintStationTaskType.PROCESS_PRINT_ORDER.value) {
                val orderId = param.toIntOrNull()
                val printOrderDTO = if (orderId == null) null else printOrderService.getPrintOrderDTO(orderId)
                if (printOrderDTO != null) {
                    param = objectMapper.writeValueAsString(printOrderDTO)
                } else {
                    continue
                }
            }

            taskDTOs += PrintStationTaskDTO(task.id, task.type, param)
        }

        return taskDTOs
    }

    @RequestMapping("/printStation/taskFetched", method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun taskFetched(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("taskId") taskId: Int): Boolean {
        return printStationService.printStationTaskFetched(sessionId, taskId)
    }

    @RequestMapping("/printStation/log", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun uploadPrintStationLog(
            @RequestParam("sessionId") sessionId: String,
            @RequestParam(name = "fileName", required = true) fileName: String,
            @RequestBody logText: String
    ): Boolean {
        return printStationService.uploadLog(sessionId, fileName, logText)
    }

    @GetMapping("/printStation/uploadLogFile")
    fun uploadLogFileConfirm(
            @RequestParam(name = "printStationId", required = true) printStationId: Int,
            modelAndView: ModelAndView
    ): ModelAndView {
        modelAndView.model["printStationId"] = printStationId
        modelAndView.viewName = "/printStation/uploadLogFile :: content"
        return modelAndView
    }

    @PostMapping("/printStation/uploadLogFile")
    @ResponseBody
    fun uploadLogFile(
            @RequestParam(name = "printStationId", required = true) printStationId: Int,
            @RequestParam(name = "logFileDate", required = true) logFileDate: String
    ): Boolean {
        return printStationService.addUploadLogFileTask(printStationId, logFileDate)
    }

    @PostMapping("/printStation/updateStatus")
    @ResponseBody
    fun updatePrintStationStatus(
            @RequestParam("sessionId") sessionId: String,
            @RequestParam("status") status : Int,
            @RequestBody additionalInfo: String
    ): Boolean {
        val statusEnum = PrintStationStatus.values().firstOrNull { it.value == status }
        return if (statusEnum == null) {
            false
        }
        else {
            printStationService.updatePrintStationStatus(sessionId, statusEnum, additionalInfo)
        }
    }

    @GetMapping("/printStation/updateAndAdSet")
    @ResponseBody
    fun getPrintStationUpdateAndAdSet(
            @RequestParam("sessionId") sessionId: String,
            @RequestParam("currentVersion") currentVersion: Int,
            @RequestParam("currentAdSetId") currentAdSetId: Int,
            @RequestParam("currentAdSetTime") currentAdSetTime: String
    ): UpdateAndAdSetDTO {
        return printStationService.getPrintStationUpdateAndAdSet(sessionId, currentVersion, currentAdSetId, currentAdSetTime)
    }

    @PostMapping("/printStation/loginWithKey")
    @ResponseBody
    fun printStationLoginWithKey(
            @RequestParam("printStationId") printStationId: Int,
            @RequestParam("version", required = false, defaultValue = "-1") version: Int,
            @RequestParam("sign") sign: String,
            @RequestParam("apiVersion", required = false, defaultValue = "1") apiVersion: Int
    ): Any {
        logger.info("PrintStation login with key, id=$printStationId, version=$version, sign=$sign")
        val versionValue = if (version > 0) version else null
        val result = printStationService.loginWithKey(printStationId, sign, versionValue)

        logger.info("PrintStation login with key, result = $result")

        return if (apiVersion > 1) {
            result
        }
        else {
            PrintStationLoginResultOld(
                    result = result.result,
                    sessionId = result.sessionId,
                    printerType = result.printerType.name,
                    resolution = result.printerType.resolution
            )
        }
    }

    @PostMapping("/printStation/initPubKey")
    @ResponseBody
    fun initPrintStationPublicKey(
            @RequestParam("printStationId") printStationId: Int,
            @RequestParam("uuid") uuid: String,
            @RequestParam("pubKey") pubKey: String
    ): Int {
        logger.info("PrintStation init public key, id=$printStationId, uuid=$uuid")
        val result = printStationService.initPublicKey(printStationId, uuid, pubKey)

        logger.info("PrintStation init public key, result = $result")
        return result
    }

    @PostMapping("/printStation/getHomeInitInfo")
    @ResponseBody
    fun getHomeInitInfo(
            @RequestParam("username") userName: String,
            @RequestParam("password") password: String,
            @RequestParam("printStationId", required = false, defaultValue = "0") printStationId: Int
    ): HomeInitInfoDTO {
        return printStationService.getHomeInitInfo(userName, password, printStationId)
    }

    @PostMapping("/printStation/initHome")
    @ResponseBody
    fun initHome(@RequestBody input: HomeInitInput): Int {
        return printStationService.initHome(input).value
    }

    @PostMapping("/printStation/reportPrinterStat")
    @ResponseBody
    fun reportPrinterStat(
            @RequestParam("sessionId") sessionId: String,
            @RequestParam("printerSerialNo") printerSn: String,
            @RequestParam("printerType") printerType: String,
            @RequestParam("printerName") printerName: String,
            @RequestParam("mediaCounter") mediaCounter: Int,
            @RequestParam("errorCode") errorCode: Int
    ): Boolean {
        return printStationService.recordPrinterStat(sessionId, printerSn, printerType, printerName, mediaCounter, errorCode)
    }


    @PostMapping("/printStation/printerInfo", consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ResponseBody
    fun updatePrintStationPrinterInfo(
            @RequestParam("sessionId") sessionId: String,
            @RequestBody printerInfo: PrinterInfoDTO
    ): Boolean {
        return printStationService.updatePrintStationPrinterInfo(sessionId, printerInfo)
    }
}

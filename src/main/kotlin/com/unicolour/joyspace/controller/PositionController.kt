package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PositionImageFileDao
import com.unicolour.joyspace.dao.PriceListDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PositionService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView

@Controller
class PositionController {
    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var positionService: PositionService

    @Autowired
    lateinit var priceListDao: PriceListDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var positionImageFileDao: PositionImageFileDao

    @GetMapping("/position/query")
    @ResponseBody
    fun positionQuery(
            @RequestParam(name = "name", required = false, defaultValue = "") name: String,
            @RequestParam(name = "companyId", required = false, defaultValue = "0") companyId: Int,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): Select2QueryResult {
        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val positions =
                if (name == "" && companyId <= 0)
                    positionDao.findAll(pageable)
                else if (name == "")
                    positionDao.findByCompanyId(companyId, pageable)
                else if (companyId <= 0)
                    positionDao.findByName(name, pageable)
                else
                    positionDao.findByCompanyIdAndName(companyId, name, pageable)

        return Select2QueryResult(
                results = positions.content.map {
                    ResultItem(
                            id = it.id,
                            text = it.name
                    )
                },
                pagination = ResultPagination(more = positions.hasNext())
        )
    }

    @RequestMapping("/position/list")
    fun positionList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val positions = if (name == null || name == "")
            positionDao.findByCompanyId(loginManager.companyId, pageable)
        else
            positionDao.findByCompanyIdAndName(loginManager.companyId, name, pageable)

        modelAndView.model.put("inputPositionName", name)

        val pager = Pager(positions.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("positions", positions.content)

        modelAndView.model.put("viewCat", "business_mgr")
        modelAndView.model.put("viewContent", "position_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/position/edit"), method = arrayOf(RequestMethod.GET))
    fun editPosition(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int
    ): ModelAndView {

        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        var position: Position? = null
        if (id > 0) {
            position = positionDao.findOne(id)
        }

        if (position == null) {
            position = Position()
        }

        modelAndView.model["create"] = id <= 0
        modelAndView.model["position"] = position
        modelAndView.model["priceLists"] = priceListDao.findByCompanyId(loginManager.companyId)
        modelAndView.viewName = "/position/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/position/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editPosition(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "shortName", required = true) shortName: String,
            @RequestParam(name = "address", required = true) address: String,
            @RequestParam(name = "transportation", required = true) transportation: String,
            @RequestParam(name = "longitudeAndLatitude", required = true) longitudeAndLatitude: String,
            @RequestParam(name = "priceListId", required = true) priceListId: Int
    ): CommonRequestResult {
        try {
            val split = longitudeAndLatitude.split(',', ' ', ';', '|', '，', '/', '\\')
            val latitude = split[0].toDouble()
            val longitude = split[1].toDouble()

            if (id <= 0) {
                positionService.createPosition(name, shortName, address, transportation, longitude, latitude, priceListId)
            } else {
                positionService.updatePosition(id, name, shortName, address, transportation, longitude, latitude, priceListId)
            }
            return CommonRequestResult()
        } catch (e: ProcessException) {
            return CommonRequestResult(e.errcode, e.message)
        } catch (e: Exception) {
            val action = if (id < 0) "创建" else "编辑"
            return CommonRequestResult(ResultCode.OTHER_ERROR.value, "${action}失败")
        }
    }

    @RequestMapping(path = arrayOf("/position/manageImageFiles"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun manageImageFiles(
            modelAndView: ModelAndView,
            @RequestParam(name = "positionId", required = true) positionId: Int): ModelAndView {

        val position = positionDao.findOne(positionId)

        modelAndView.model.put("position", position)

        val images = positionImageFileDao.findByPositionId(positionId)

        modelAndView.model.put("images", images.map {
            PreviewImageFileDTO("${baseUrl}/assets/position/images/${it.id}.${it.fileType}", it.id)
        })

        modelAndView.viewName = "/position/edit :: manageImageFiles"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/position/uploadImageFile"), method = arrayOf(RequestMethod.POST))
    fun uploadPositionImages(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam("imageFile") imageFile: MultipartFile?
    ): ModelAndView {
        val uploadedImgFile = positionService.uploadPositionImageFile(id, imageFile)

        if (uploadedImgFile != null) {
            modelAndView.model["imageUploadDivId"] = "imgUpload"
            modelAndView.viewName = "/position/imageFileUploaded"
            modelAndView.model["uploadedImg"] = PreviewImageFileDTO("${baseUrl}/assets/position/images/${uploadedImgFile.id}.${uploadedImgFile.fileType}", uploadedImgFile.id)
        }

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/position/deleteImageFile"), method = arrayOf(RequestMethod.POST))
    fun deletePositionImages(
            modelAndView: ModelAndView,
            @RequestParam(name = "imgFileId", required = true) imgFileId: Int): ModelAndView {
        positionService.deletePositionImageFile(imgFileId)
        modelAndView.model["imgFileId"] = imgFileId
        modelAndView.viewName = "/position/imageFileDeleted"
        return modelAndView
    }
}
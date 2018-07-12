package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.AdSetDao
import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dto.AdSetImageDTO
import com.unicolour.joyspace.model.AdSet
import com.unicolour.joyspace.service.AdSetService
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import java.nio.charset.StandardCharsets
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Controller
class AdSetController {
    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var adSetService: AdSetService

    @Autowired
    lateinit var companyDao: CompanyDao

    @RequestMapping("/adSet/list")
    fun adSetList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager
        val isSuperAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")
        val companyId = if (isSuperAdmin) null else loginManager!!.companyId

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.DESC, "id")
        val adSets = adSetDao.queryAdSets(pageable, companyId, name ?: "", true)

        val companyIdNameMap = HashMap<Int, String>()
        val ads = adSets.map {
            AdSetInfo(
                    adSet = it,
                    companyName = if (it.companyId <= 0) "" else companyIdNameMap.computeIfAbsent(it.companyId, { companyId ->
                       companyDao.findOne(companyId)?.name ?: ""
                    }),
                    adImageFiles = it.imageFiles.sortedBy { it.sequence }.map { imgFile ->
                        AdImageFileInfo(
                                id = imgFile.id,
                                url = adSetService.getAdThumbImageUrl(imgFile),
                                duration = imgFile.duration,
                                width = imgFile.width,
                                height = imgFile.height
                        )
                    },
                    editable = isSuperAdmin || it.companyId == loginManager!!.companyId
            )
        }
        modelAndView.model.put("inputAdSetName", name)

        val pager = Pager(adSets.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("adSets", ads)

        modelAndView.model.put("viewCat", "business_mgr")
        modelAndView.model.put("viewContent", "adSet_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/adSet/edit"), method = arrayOf(RequestMethod.GET))
    fun editAdSet(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val loginManager = managerService.loginManager

        val rows = ArrayList<AdSetImageDTO>()

        var adSet: AdSet? = null
        var index = 0
        if (id > 0) {
            adSet = adSetDao.findOne(id)
            rows.addAll(adSet.imageFiles.sortedBy { it.sequence }.map {
                AdSetImageDTO(
                        index = index++,
                        adImgId = it.id,
                        url = adSetService.getAdThumbImageUrl(it),
                        duration = it.duration,
                        enabled = it.enabled
                )
            })
        }

        for (i in index..99) {
            rows.add(AdSetImageDTO(
                    index = index++,
                    adImgId = 0,
                    url = "",
                    duration = 5
            ))
        }

        if (adSet == null) {
            val now = Calendar.getInstance()

            adSet = AdSet()
            adSet.name = ""
            adSet.createTime = now
            adSet.updateTime = now
            adSet.imageFiles = emptyList()
            adSet.companyId = loginManager!!.companyId
        }

        modelAndView.model["create"] = id <= 0
        modelAndView.model["adSet"] = adSet
        modelAndView.model["rows"] = rows
        modelAndView.viewName = "/adSet/edit :: content"

        return modelAndView
    }

    private fun readPartValue(part: Part): String? {
        return if (part.size == 0L) {
            null
        }
        else {
            part.inputStream.readBytes().toString(StandardCharsets.UTF_8)
        }
    }

    @RequestMapping(path = arrayOf("/adSet/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editAdSet(
            modelAndView: ModelAndView,
            request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "publicResource", required = false, defaultValue = "false") publicResource: Boolean,
            @RequestParam(name = "name", required = true) name: String): ModelAndView {

        val adImageFiles = ArrayList<AdSetImageDTO>()
        for (i in 0..99) {
            val adImgIdPart = request.getPart("adImgId_$i")
            val durationPart = request.getPart("duration_$i")
            val uploadFileNamePart = request.getPart("uploadFileName_$i")
            val sequencePart = request.getPart("sequence_$i")
            val enabledPart = request.getPart("enabled_$i")

            if (adImgIdPart != null && durationPart != null && uploadFileNamePart != null && sequencePart != null) {
                adImageFiles += AdSetImageDTO(
                        index = i,
                        adImgId = readPartValue(adImgIdPart)?.toInt() ?: 0,
                        duration = readPartValue(durationPart)?.toInt() ?: 0,
                        uploadFileName = readPartValue(uploadFileNamePart) ?: "",
                        sequence = readPartValue(sequencePart)?.toInt() ?: 0,
                        enabled = if (enabledPart == null) false else readPartValue(enabledPart) == "on"
                )
            }
        }
        adImageFiles.sortBy { it.sequence }

        if (id <= 0) {
            adSetService.createAdSet(name, publicResource, adImageFiles)
        } else {
            adSetService.updateAdSet(id, name, publicResource, adImageFiles)
        }

        modelAndView.viewName = "adSet/adSetEditCompleted"
        return modelAndView
    }

    @PostMapping("/adSet/uploadImageFile")
    fun uplaodAdSetImage(modelAndView: ModelAndView, @RequestParam("imageFile") imageFile: MultipartFile?): ModelAndView {
        val fileNameAndThumbUrl = adSetService.uploadAdSetImageFile(imageFile)

        if (fileNameAndThumbUrl != null) {
            modelAndView.viewName = "/adSet/imageFileUploaded"
            modelAndView.model["tempAdSetImgFileName"] = fileNameAndThumbUrl[0]
            modelAndView.model["thumbUrl"] = fileNameAndThumbUrl[1]
        }

        return modelAndView
    }

    @GetMapping("/adSet/preview/{adSetId}")
    fun previewAdSet(modelAndView: ModelAndView, @PathVariable("adSetId") adSetId: Int): ModelAndView {
        val loginManager = managerService.loginManager
        val isSuperAdmin = managerService.loginManagerHasRole("ROLE_SUPERADMIN")

        val adSet = adSetDao.findOne(adSetId)
        if (adSet == null || adSet.companyId > 0 && adSet.companyId != loginManager!!.companyId && !isSuperAdmin) {
            modelAndView.viewName = "empty"
        }
        else {
            modelAndView.model["adName"] = adSet.name
            modelAndView.model["adImages"] = adSet.imageFiles.sortedBy { it.sequence }.map { imgFile ->
                AdImageFileInfo(
                        id = imgFile.id,
                        url = adSetService.getAdImageUrl(imgFile),
                        duration = imgFile.duration,
                        width = imgFile.width,
                        height = imgFile.height
                )
            }

            modelAndView.viewName = "/adSet/preview"
        }

        return modelAndView
    }
}

internal class AdImageFileInfo(val id: Int, val duration: Int, val url: String, val width: Int, val height: Int)
internal class AdSetInfo(val adSet: AdSet, val adImageFiles: List<AdImageFileInfo>, val editable:Boolean, val companyName: String)

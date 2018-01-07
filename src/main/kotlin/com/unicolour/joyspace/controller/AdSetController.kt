package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.AdSetService
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
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.Part
import kotlin.collections.ArrayList

@Controller
class AdSetController {
    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var adSetService: AdSetService

    @RequestMapping("/adSet/list")
    fun adSetList(
            request: HttpServletRequest,
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val baseUrl = getBaseUrl(request)
        val loginManager = managerService.loginManager

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.DESC, "id")
        val adSets = if (name == null || name == "")
            adSetDao.findByCompanyId(loginManager!!.companyId, pageable)
        else
            adSetDao.findByNameIgnoreCaseAndCompanyId(name, loginManager!!.companyId, pageable)

        class AdSetInfo(val adSet: AdSet, val adImageFiles: List<AdImageFileInfo>)

        val ads = adSets.map {
            AdSetInfo(
                    adSet = it,
                    adImageFiles = it.imageFiles.map { imgFile ->
                        AdImageFileInfo(
                                id = imgFile.id,
                                url = adSetService.getAdImageUrl(baseUrl, imgFile),
                                duration = imgFile.duration
                        )
                    }
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
            request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        val baseUrl = getBaseUrl(request)
        val loginManager = managerService.loginManager

        var adSet: AdSet? = null
        var imageFiles: List<AdImageFileInfo> = emptyList()
        if (id > 0) {
            adSet = adSetDao.findOne(id)
            imageFiles = adSet.imageFiles.map {
                AdImageFileInfo(
                        id = it.id,
                        url = adSetService.getAdImageUrl(baseUrl, it),
                        duration = it.duration
                )
            }
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

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("adSet", adSet)
        modelAndView.model.put("adImageFiles", imageFiles)
        modelAndView.viewName = "/adSet/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/adSet/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editAdSet(
            request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String): Boolean {

        val adImageFiles = ArrayList<Pair<Part, Int>>()
        for (i in 1 .. 100) {
            val imgFile = request.getPart("imageFile$i")
            if (imgFile.size > 0) {
                val duration = request.getPart("duration$i")
                if (duration.size > 0) {
                    val durationVal = duration.inputStream.use { it.reader().readText().toInt() }
                    if (durationVal > 0) {
                        adImageFiles.add(imgFile to durationVal)
                    }
                }
            }
        }

        if (id <= 0) {
            adSetService.createAdSet(name, adImageFiles)
            return true
        } else {
            return adSetService.updateAdSet(id, name, adImageFiles)
        }
    }
}

class AdImageFileInfo(val id: Int, val duration: Int, val url: String)

package com.unicolour.joyspace.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.TemplateDao
import com.unicolour.joyspace.dto.IDPhotoParam
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.model.Template
import com.unicolour.joyspace.service.TemplateService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView

@Controller
class TemplateController {

    @Autowired
    lateinit var templateService: TemplateService

    @Autowired
    lateinit var templateDao: TemplateDao

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @RequestMapping("/template/list")
    fun templateList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "type", required = false, defaultValue = "-1") type: Int,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val nameIsNullOrBlank = name.isNullOrBlank()
        val templates =
                if (nameIsNullOrBlank && type < 0)
                    templateDao.findAll(pageable)
                else if (!nameIsNullOrBlank && type >= 0)
                    templateDao.findByNameAndType(name!!, type, pageable)
                else if (type >= 0)
                    templateDao.findByType(type, pageable)
                else
                    templateDao.findByName(name!!, pageable)

        class TemplateWrapper(val template: Template, val idPhotoSize: String)

        val templateWrappers = templates.content.map {
            var idPhotoSize = ""
            if (it.type == ProductType.ID_PHOTO.value && !it.tplParam.isNullOrBlank()) {
                try {
                    val idPhotoParam = objectMapper.readValue(it.tplParam, IDPhotoParam::class.java)
                    idPhotoSize = String.format("%.2f", idPhotoParam.elementWidth) + " x " + String.format("%.2f", idPhotoParam.elementHeight)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            TemplateWrapper(it, idPhotoSize)
        }

        modelAndView.model["inputTemplateName"] = name
        modelAndView.model["inputTemplateType"] = type

        val pager = Pager(templates.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        modelAndView.model["templates"] = templateWrappers

        modelAndView.model["viewCat"] = "product_mgr"
        modelAndView.model["viewContent"] = "template_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/template/edit"), method = arrayOf(RequestMethod.GET))
    fun editTemplate(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "type", required = true) type: Int): ModelAndView {
        var template: Template? = null
        if (id > 0) {
            template = templateDao.findOne(id)
        }

        if (template == null) {
            template = Template()
            template.width = 0.0
            template.height = 0.0
        }

        modelAndView.model["templates"] = templateDao.findAll()

        modelAndView.model["create"] = id <= 0
        modelAndView.model["template"] = template

        if (type == ProductType.ID_PHOTO.value) {
            val idPhotoParam =
                    if (id > 0 && !template.tplParam.isNullOrBlank()) {
                        try {
                            objectMapper.readValue(template.tplParam, IDPhotoParam::class.java)
                        } catch (e: Exception) {
                            IDPhotoParam()
                        }
                    }
                    else {
                        IDPhotoParam()
                    }

            modelAndView.model["idPhotoParam"] = idPhotoParam
            modelAndView.viewName = "/template/idPhotoEdit :: content"
        }
        else if (type == ProductType.PHOTO.value) {
            modelAndView.viewName = "/template/photoEdit :: content"
        }
        else {
            modelAndView.viewName = "/template/edit :: content"
        }

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/template/edit"), method = arrayOf(RequestMethod.POST))
    fun editTemplate(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam("templateFile") templateFile: MultipartFile?
    ): ModelAndView {

        val success: Boolean
        if (id <= 0) {
            templateService.createTemplate(name, ProductType.TEMPLATE, templateFile!!)
            success = true
        } else {
            success = templateService.updateTemplate(id, name, ProductType.TEMPLATE, templateFile)
        }

        modelAndView.model["success"] = success
        modelAndView.viewName = "/template/templateFileUploaded"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/template/editIdPhoto"), method = arrayOf(RequestMethod.POST))
    fun editIdPhotoTemplate(
            modelAndView: ModelAndView,
            @RequestParam(name = "preview", required = true) preview: Boolean,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "tplWidth", required = true) tplWidth: Double,
            @RequestParam(name = "tplHeight", required = true) tplHeight: Double,
            @RequestParam(name = "elementWidth", required = true) elementWidth: Double,
            @RequestParam(name = "elementHeight", required = true) elementHeight: Double,
            @RequestParam(name = "rowCount", required = true) rowCount: Int,
            @RequestParam(name = "columnCount", required = true) columnCount: Int,
            @RequestParam(name = "horGap", required = true) horGap: Double,
            @RequestParam(name = "verGap", required = true) verGap: Double,
            @RequestParam(name = "gridLineWidth", required = true) gridLineWidth: Double?,
            @RequestParam("maskImageFile") maskImageFile: MultipartFile?
    ): ModelAndView {

        val idPhotoParam = IDPhotoParam()
        idPhotoParam.elementWidth = elementWidth
        idPhotoParam.elementHeight = elementHeight
        idPhotoParam.rowCount = rowCount
        idPhotoParam.columnCount = columnCount
        idPhotoParam.horGap = horGap
        idPhotoParam.verGap = verGap
        idPhotoParam.gridLineWidth = gridLineWidth ?: 0.0

        if (preview) {
            val svg = templateService.previewIDPhotoTemplate(tplWidth, tplHeight,idPhotoParam, maskImageFile)

            modelAndView.model["svg"] = svg
            modelAndView.viewName = "/template/preview"
        }
        else {
            val success: Boolean
            if (id <= 0) {
                templateService.createIDPhotoTemplate(name, tplWidth, tplHeight, idPhotoParam, maskImageFile)
                success = true
            } else {
                success = templateService.updateIDPhotoTemplate(id, name, tplWidth, tplHeight, idPhotoParam, maskImageFile)
            }

            modelAndView.model["success"] = success
            modelAndView.viewName = "/template/templateFileUploaded"
        }

        return modelAndView
    }

    @PostMapping("/template/editPhoto")
    fun editPhotoTemplate(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "tplWidth", required = true) tplWidth: Double,
            @RequestParam(name = "tplHeight", required = true) tplHeight: Double
    ): ModelAndView {

        val success: Boolean
        if (id <= 0) {
            templateService.createPhotoTemplate(name, tplWidth, tplHeight)
            success = true
        } else {
            success = templateService.updatePhotoTemplate(id, name, tplWidth, tplHeight)
        }

        modelAndView.model["success"] = success
        modelAndView.viewName = "/template/templateFileUploaded"

        return modelAndView
    }
}
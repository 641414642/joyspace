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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
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

        modelAndView.model.put("inputTemplateName", name)
        modelAndView.model.put("inputTemplateType", type)

        val pager = Pager(templates.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("templates", templates.content)

        modelAndView.model.put("viewCat", "product_mgr")
        modelAndView.model.put("viewContent", "template_list")
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
            template.width = 101.60
            template.height = 152.40
        }

        modelAndView.model["templates"] = templateDao.findAll()
        modelAndView.model["types"] = ProductType.values()

        modelAndView.model["create"] = id <= 0
        modelAndView.model["template"] = template

        if (type == ProductType.ID_PHOTO.value) {
            val idPhotoParam =
                    if (id > 0 && !template.tplParam.isNullOrBlank()) {
                        try {
                            objectMapper.readValue(template.tplParam, IDPhotoParam::class.java)
                        } catch (e: Exception) {
                            templateService.createDefaultIDPhotoParam()
                        }
                    }
                    else {
                        templateService.createDefaultIDPhotoParam()
                    }

            modelAndView.model["idPhotoParam"] = idPhotoParam
            modelAndView.viewName = "/template/idPhotoEdit :: content"
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
            @RequestParam(name = "type", required = true) type: Int,
            @RequestParam("templateFile") templateFile: MultipartFile?
    ): ModelAndView {

        val productType = ProductType.values().find{ it.value == type }
        val success: Boolean
        if (id <= 0) {
            templateService.createTemplate(name, productType!!, templateFile!!)
            success = true
        } else {
            success = templateService.updateTemplate(id, name, productType!!, templateFile)
        }

        modelAndView.model["success"] = success
        modelAndView.viewName = "/template/templateFileUploaded"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/template/editIdPhoto"), method = arrayOf(RequestMethod.POST))
    fun editIdPhotoTemplate(
            modelAndView: ModelAndView,
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
            @RequestParam(name = "gridLineWidth", required = true) gridLineWidth: Double,
            @RequestParam("maskImageFile") maskImageFile: MultipartFile?
    ): ModelAndView {

        val productType = ProductType.ID_PHOTO
        val success: Boolean
        if (id <= 0) {
            templateService.createTemplate(name, productType!!, templateFile!!)
            success = true
        } else {
            success = templateService.updateTemplate(id, name, productType!!, templateFile)
        }

        modelAndView.model["success"] = success
        modelAndView.viewName = "/template/templateFileUploaded"

        return modelAndView
    }
}
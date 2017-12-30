package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.TemplateDao
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
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView

@Controller
class TemplateController {

    @Autowired
    lateinit var templateService: TemplateService

    @Autowired
    lateinit var templateDao: TemplateDao

    @RequestMapping("/template/list")
    fun templateList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.ASC, "id")
        val templates = if (name == null || name == "")
            templateDao.findAll(pageable)
        else
            templateDao.findByName(name, pageable)

        modelAndView.model.put("inputTemplateName", name)

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
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var template: Template? = null
        if (id > 0) {
            template = templateDao.findOne(id)
        }

        if (template == null) {
            template = Template()
        }

        modelAndView.model["templates"] = templateDao.findAll()
        modelAndView.model["types"] = ProductType.values()

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("template", template)
        modelAndView.viewName = "/template/edit :: content"

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
        var success = false
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
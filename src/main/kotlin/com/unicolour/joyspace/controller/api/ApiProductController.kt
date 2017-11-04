package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dto.PreviewParam
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.service.TemplateService
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RestController
class ApiProductController {
    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var templateService: TemplateService

    @RequestMapping("/api/product/preview", method = arrayOf(RequestMethod.POST))
    fun productPreview(
            request: HttpServletRequest,
            @RequestBody previewParam: PreviewParam): ResponseEntity<TemplatePreviewResult> {

        val baseUrl = getBaseUrl(request)
        return ResponseEntity.ok(
                templateService.createPreview(previewParam, baseUrl))
    }
}

package com.unicolour.joyspace.service

import com.unicolour.joyspace.controller.api.PreviewParam
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.model.Template
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface TemplateService {
    fun createPreview(previewParam: PreviewParam, template: Template, baseUrl: String): TemplatePreviewResult
    fun createTemplate(name: String, type: ProductType, templateFile: MultipartFile)
    fun updateTemplate(id: Int, name: String, type: ProductType, templateFile: MultipartFile?): Boolean
    fun getTemplateImageDataFetcher(fieldName:String): DataFetcher<Any>

    val templateFileUrlDataFetcher: DataFetcher<String?>
    val templatesDataFetcher: DataFetcher<List<Template>>
}

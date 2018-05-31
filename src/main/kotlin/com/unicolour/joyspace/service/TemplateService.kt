package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.IDPhotoParam
import com.unicolour.joyspace.dto.PreviewParam
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.model.Template
import graphql.schema.DataFetcher
import org.springframework.data.domain.Page
import org.springframework.web.multipart.MultipartFile
import javax.transaction.Transactional

interface TemplateService {
    fun createPreview(previewParam: PreviewParam): TemplatePreviewResult
    fun createTemplate(name: String, type: ProductType, templateFile: MultipartFile)
    fun updateTemplate(id: Int, name: String, type: ProductType, templateFile: MultipartFile?): Boolean
    fun getTemplateImageDataFetcher(fieldName:String): DataFetcher<Any>

    fun createIDPhotoTemplate(name: String, tplWidth: Double, tplHeight: Double, idPhotoParam: IDPhotoParam, maskImageFile: MultipartFile?)
    fun updateIDPhotoTemplate(id: Int, name: String, tplWidth: Double, tplHeight: Double, idPhotoParam: IDPhotoParam, maskImageFile: MultipartFile?): Boolean
    fun previewIDPhotoTemplate(tplWidth: Double, tplHeight: Double, idPhotoParam: IDPhotoParam, maskImageFile: MultipartFile?): String

    fun queryTemplates(pageNo: Int, pageSize: Int, type: ProductType?, name: String, excludeDeleted: Boolean, order: String): Page<Template>
    fun queryTemplates(type: ProductType?, name: String, excludeDeleted: Boolean, order: String): List<Template>
    fun createPhotoTemplate(name: String, tplWidth: Double, tplHeight: Double)
    fun updatePhotoTemplate(id: Int, name: String, tplWidth: Double, tplHeight: Double): Boolean
    fun deleteTemplateById(templateId: Int): Boolean

    val templateFileUrlDataFetcher: DataFetcher<String?>
    val templatesDataFetcher: DataFetcher<List<Template>>
}

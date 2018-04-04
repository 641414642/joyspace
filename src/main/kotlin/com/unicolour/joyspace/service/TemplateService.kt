package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.IDPhotoParam
import com.unicolour.joyspace.dto.PreviewParam
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.model.Template
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface TemplateService {
    fun createPreview(previewParam: PreviewParam): TemplatePreviewResult
    fun createTemplate(name: String, type: ProductType, templateFile: MultipartFile)
    fun updateTemplate(id: Int, name: String, type: ProductType, templateFile: MultipartFile?): Boolean
    fun getTemplateImageDataFetcher(fieldName:String): DataFetcher<Any>

    fun createIDPhotoTemplate(name: String, tplWidth: Double, tplHeight: Double, idPhotoParam: IDPhotoParam, maskImageFile: MultipartFile?)
    fun updateIDPhotoTemplate(id: Int, name: String, tplWidth: Double, tplHeight: Double, idPhotoParam: IDPhotoParam, maskImageFile: MultipartFile?): Boolean
    fun previewIDPhotoTemplate(tplWidth: Double, tplHeight: Double, idPhotoParam: IDPhotoParam, maskImageFile: MultipartFile?): String

    fun createPhotoTemplate(name: String, tplWidth: Double, tplHeight: Double)
    fun updatePhotoTemplate(id: Int, name: String, tplWidth: Double, tplHeight: Double): Boolean

    val templateFileUrlDataFetcher: DataFetcher<String?>
    val templatesDataFetcher: DataFetcher<List<Template>>
}

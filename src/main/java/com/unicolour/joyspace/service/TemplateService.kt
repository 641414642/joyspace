package com.unicolour.joyspace.service

import com.unicolour.joyspace.controller.api.PreviewParam
import com.unicolour.joyspace.dto.TemplateInfo
import com.unicolour.joyspace.dto.TemplatePreviewResult

interface TemplateService {
    fun getTemplateNames(): List<String>
    fun getTemplateInfo(templateName: String) : TemplateInfo?
    fun createPreview(previewParam: PreviewParam, templateName: String, baseUrl: String): TemplatePreviewResult
}

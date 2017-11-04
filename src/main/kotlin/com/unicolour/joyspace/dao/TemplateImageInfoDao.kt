package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.TemplateImageInfo
import org.springframework.data.repository.CrudRepository

interface TemplateImageInfoDao : CrudRepository<TemplateImageInfo, Int> {
    fun findByTemplateIdAndTemplateVersion(templateId: Int, templateVersion: Int): List<TemplateImageInfo>
}
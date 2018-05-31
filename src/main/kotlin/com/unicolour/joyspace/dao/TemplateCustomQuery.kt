package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.model.Template
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

interface TemplateCustomQuery {
    fun queryTemplates(pageable: Pageable, type: ProductType?, name: String, excludeDeleted: Boolean): Page<Template>
    fun queryTemplates(type: ProductType?, name: String, excludeDeleted: Boolean, sort: Sort): List<Template>
}

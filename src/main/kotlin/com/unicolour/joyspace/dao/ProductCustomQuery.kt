package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

interface ProductCustomQuery {
    fun queryProducts(pageable: Pageable, companyId: Int, name: String, excludeDeleted: Boolean): Page<Product>
    fun queryProducts(companyId: Int, name: String, excludeDeleted: Boolean, sort: Sort): List<Product>
}
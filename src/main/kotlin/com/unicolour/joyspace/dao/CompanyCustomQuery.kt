package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.BusinessModel
import com.unicolour.joyspace.model.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CompanyCustomQuery {
    fun queryCompanies(pageable: Pageable, name: String, businessModel: BusinessModel?): Page<Company>
    fun queryCompanies(name: String, businessModel: BusinessModel?): List<Company>
}

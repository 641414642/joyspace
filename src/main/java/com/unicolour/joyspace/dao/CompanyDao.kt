package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface CompanyDao : PagingAndSortingRepository<Company, Int> {
    fun findByName(name: String, pageable: Pageable) : Page<Company>
}

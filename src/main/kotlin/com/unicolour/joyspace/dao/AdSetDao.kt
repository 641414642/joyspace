package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AdSet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface AdSetDao : PagingAndSortingRepository<AdSet, Int> {
    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<AdSet>
    fun findByNameIgnoreCaseAndCompanyId(name: String, companyId: Int, pageable: Pageable): Page<AdSet>
}

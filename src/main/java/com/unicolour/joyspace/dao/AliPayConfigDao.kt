package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AliPayConfig
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface AliPayConfigDao : PagingAndSortingRepository<AliPayConfig, Int> {
    fun findByName(name: String, pageable: Pageable): Page<AliPayConfig>
}

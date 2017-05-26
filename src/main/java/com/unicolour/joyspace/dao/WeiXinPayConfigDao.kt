package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.WeiXinPayConfig
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface WeiXinPayConfigDao : PagingAndSortingRepository<WeiXinPayConfig, Int> {
    fun findByName(name: String, pageable: Pageable): Page<WeiXinPayConfig>
}

package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.WeiXinPayConfig
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface WeiXinPayConfigDao : PagingAndSortingRepository<WeiXinPayConfig, Int> {
    @Query("SELECT w FROM WeiXinPayConfig w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable): Page<WeiXinPayConfig>
}

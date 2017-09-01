package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AliPayConfig
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface AliPayConfigDao : PagingAndSortingRepository<AliPayConfig, Int> {
    @Query("SELECT a FROM AliPayConfig a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable): Page<AliPayConfig>
}

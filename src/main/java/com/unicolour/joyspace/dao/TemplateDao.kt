package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Template
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface TemplateDao : PagingAndSortingRepository<Template, Int> {
    @Query("SELECT t FROM Template t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable): Page<Template>
}
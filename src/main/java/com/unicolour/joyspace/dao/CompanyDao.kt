package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Company
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface CompanyDao : PagingAndSortingRepository<Company, Int> {
    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable) : Page<Company>
}

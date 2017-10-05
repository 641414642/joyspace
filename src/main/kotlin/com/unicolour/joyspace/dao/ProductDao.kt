package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface ProductDao : PagingAndSortingRepository<Product, Int> {
    @Query("SELECT p FROM Product p WHERE p.companyId=:companyId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByCompanyIdAndName(@Param("companyId") companyId: Int, @Param("name") name: String, pageable: Pageable): Page<Product>

    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<Product>
    fun findByTemplateId(id: Int): List<Product>
}
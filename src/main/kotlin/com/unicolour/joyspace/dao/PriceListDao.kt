package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PriceList
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface PriceListDao : PagingAndSortingRepository<PriceList, Int> {
    @Query("SELECT p FROM PriceList p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByName(@Param("name") name: String, pageable: Pageable): Page<PriceList>

    @Query("SELECT p FROM PriceList p WHERE p.companyId=:companyId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByCompanyIdAndName(@Param("companyId") companyId: Int, @Param("name") name: String, pageable: Pageable): Page<PriceList>

    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<PriceList>
}

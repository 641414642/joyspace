package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Position
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface PositionDao : PagingAndSortingRepository<Position, Int> {
    @Query("SELECT p FROM Position p WHERE p.companyId=:companyId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun findByCompanyIdAndName(@Param("companyId") companyId: Int, @Param("name") name: String, pageable: Pageable): Page<Position>

    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<Position>
}

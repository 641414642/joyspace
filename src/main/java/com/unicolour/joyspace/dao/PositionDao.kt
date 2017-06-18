package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Position
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface PositionDao : PagingAndSortingRepository<Position, Int> {
    fun findByName(name: String, pageable: Pageable): Page<Position>
}

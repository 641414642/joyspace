package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PriceList
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface PriceListDao : PagingAndSortingRepository<PriceList, Int> {
    fun findByName(name: String, pageable: Pageable): Page<PriceList>
}

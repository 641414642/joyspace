package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface ProductDao : PagingAndSortingRepository<Product, Int> {
    fun findByName(name: String, pageable: Pageable): Page<Product>
}

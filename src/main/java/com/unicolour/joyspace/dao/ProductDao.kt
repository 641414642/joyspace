package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.Product
import org.springframework.data.repository.PagingAndSortingRepository

interface ProductDao : PagingAndSortingRepository<Product, Int> {
}

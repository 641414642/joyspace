package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.TPriceItem
import org.springframework.data.repository.CrudRepository

interface TPriceItemDao : CrudRepository<TPriceItem, Int> {

    fun findByTPriceIdOrderByIdAsc(TPriceId: Int): List<TPriceItem>

    fun deleteById(TPriceItemId: Int)


}

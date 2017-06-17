package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PriceListItem
import org.springframework.data.repository.CrudRepository

interface PriceListItemDao : CrudRepository<PriceListItem, Int> {
    fun findByPriceListId(priceListId: Int) : List<PriceListItem>
    fun deleteByPriceListId(id: Int) : Int
}
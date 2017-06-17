package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.model.PriceListItem

interface PriceListService {
    fun getPriceListItems(priceListId: Int?): List<PriceListItem>
    fun createPriceList(name: String, productIdPriceMap: Map<Int, String>) : PriceList?
    fun updatePriceList(id: Int, name: String, productIdPriceMap: Map<Int, String>): Boolean
}
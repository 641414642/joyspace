package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PriceListItem

interface PriceListService {
    fun getPriceListItems(priceListId: Int?): List<PriceListItem>
}
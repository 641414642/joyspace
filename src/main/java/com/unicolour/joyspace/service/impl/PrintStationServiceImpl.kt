package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.PriceListService
import com.unicolour.joyspace.service.PrintStationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PrintStationServiceImpl : PrintStationService {
    @Autowired
    lateinit var priceListService: PriceListService

    override fun getPriceMap(printStation: PrintStation?): Map<Int, Int> {
        val priceListItems: List<PriceListItem> = priceListService.getPriceListItems(printStation?.position?.priceListId)
        val defPriceListItems: List<PriceListItem> = priceListService.getPriceListItems(printStation?.company?.defaultPriceListId)

        val priceMap: MutableMap<Int, Int> = HashMap()
        for (priceListItem in defPriceListItems) {
            priceMap[priceListItem.productId] = priceListItem.price
        }

        for (priceListItem in priceListItems) {
            priceMap[priceListItem.productId] = priceListItem.price
        }

        return priceMap;
    }
}


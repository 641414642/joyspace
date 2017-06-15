package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PriceListItemDao
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.PrintStationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PrintStationServiceImpl : PrintStationService {
    @Autowired
    lateinit var priceListItemDao: PriceListItemDao

    override fun getPriceMap(printStation: PrintStation?): Map<Int, Int> {
        val priceListItems: List<PriceListItem> =
            if (printStation == null || printStation.position.priceListId <= 0) emptyList()
            else priceListItemDao.findByPriceListId(printStation.position.priceListId)

        return priceListItems.associateBy( { it.productId  }, { it.price })
    }
}


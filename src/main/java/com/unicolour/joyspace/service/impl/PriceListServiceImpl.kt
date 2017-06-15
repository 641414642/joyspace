package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PriceListItemDao
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.service.PriceListService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PriceListServiceImpl : PriceListService {
    @Autowired
    lateinit var priceListItemDao: PriceListItemDao

    override fun getPriceListItems(priceListId: Int?): List<PriceListItem> {
        if (priceListId == null) {
            return emptyList()
        }
        else {
            return priceListItemDao.findByPriceListId(priceListId)
        }
    }
}
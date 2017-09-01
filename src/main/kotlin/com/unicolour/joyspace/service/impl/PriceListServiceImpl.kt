package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dao.PriceListDao
import com.unicolour.joyspace.dao.PriceListItemDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PriceListService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
open class PriceListServiceImpl : PriceListService {
    @Autowired
    lateinit var priceListItemDao: PriceListItemDao

    @Autowired
    lateinit var priceListDao: PriceListDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var productDao: ProductDao

    override fun getPriceListItems(priceListId: Int?): List<PriceListItem> {
        if (priceListId == null) {
            return emptyList()
        }
        else {
            return priceListItemDao.findByPriceListId(priceListId)
        }
    }

    @Transactional
    override fun createPriceList(name: String, productIdPriceMap: Map<Int, String>): PriceList? {
        val loginManager = managerService.loginManager
        if (loginManager == null) {
            return null
        }

        val manager = managerDao.findOne(loginManager.managerId)

        val priceList = PriceList()
        priceList.name = name;
        priceList.createTime = Calendar.getInstance()
        priceList.company = manager.company;

        priceListDao.save(priceList)

        for (entry in productIdPriceMap) {
            val productId = entry.key
            val price = entry.value.toDoubleOrNull();

            if (price != null) {
                val priceListItem = PriceListItem();
                priceListItem.price = (price * 100).toInt()
                priceListItem.product = productDao.findOne(productId)
                priceListItem.priceList = priceList

                priceListItemDao.save(priceListItem);
            }
        }

        return priceList
    }

    @Transactional
    override fun updatePriceList(id: Int, name: String, productIdPriceMap: Map<Int, String>): Boolean {
        val priceList = priceListDao.findOne(id)
        if (priceList != null) {
            priceList.name = name;

            priceListDao.save(priceList)

            priceListItemDao.deleteByPriceListId(id)

            for (entry in productIdPriceMap) {
                val productId = entry.key
                val price = entry.value.toDoubleOrNull();

                if (price != null) {
                    val priceListItem = PriceListItem();
                    priceListItem.price = (price * 100).toInt()
                    priceListItem.product = productDao.findOne(productId)
                    priceListItem.priceList = priceList

                    priceListItemDao.save(priceListItem);
                }
            }

            return true
        }
        else {
            return false
        }
    }
}
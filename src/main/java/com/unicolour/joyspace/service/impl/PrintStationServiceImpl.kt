package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PriceListService
import com.unicolour.joyspace.service.PrintStationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
open class PrintStationServiceImpl : PrintStationService {
    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var managerDao : ManagerDao

    @Autowired
    lateinit var positionDao : PositionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var priceListService: PriceListService

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

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

    @Transactional
    override fun createPrintStation(sn: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation? {
        val loginManager = managerService.loginManager
        val manager = managerDao.findOne(loginManager.managerId)

        val printStation = PrintStation()
        printStation.sn = sn
        printStation.wxQrCode = wxQrCode
        printStation.company = manager.company
        printStation.position = positionDao.findOne(positionId)

        printStationDao.save(printStation)

        for (productId in selectedProductIds) {
            val printStationProduct = PrintStationProduct()
            printStationProduct.product = productDao.findOne(productId)
            printStationProduct.printStation = printStation

            printStationProductDao.save(printStationProduct);
        }

        return printStation
    }

    @Transactional
    override fun updatePrintStation(id: Int, sn: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): Boolean {
        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            printStation.sn = sn
            printStation.wxQrCode = wxQrCode
            printStation.position = positionDao.findOne(positionId)

            printStationDao.save(printStation)

            printStationProductDao.deleteByPrintStationId(id)

            for (productId in selectedProductIds) {
                val printStationProduct = PrintStationProduct()
                printStationProduct.product = productDao.findOne(productId)
                printStationProduct.printStation = printStation

                printStationProductDao.save(printStationProduct);
            }
            return true
        }
        else {
            return false
        }
    }
}


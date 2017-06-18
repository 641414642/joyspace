package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.model.PrintStation
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
    override fun createPrintStation(sn: String, wxQrCode: String, positionId: Int): PrintStation? {
        val loginManager = managerService.loginManager
        val manager = managerDao.findOne(loginManager.managerId)

        val printStation = PrintStation()
        printStation.sn = sn
        printStation.wxQrCode = wxQrCode
        printStation.company = manager.company
        printStation.position = positionDao.findOne(positionId)

        printStationDao.save(printStation)
        return printStation
    }

    @Transactional
    override fun updatePrintStation(id: Int, sn: String, wxQrCode: String, positionId: Int): Boolean {
        val printStation = printStationDao.findOne(id)

        if (printStation != null) {
            printStation.sn = sn
            printStation.wxQrCode = wxQrCode
            printStation.position = positionDao.findOne(positionId)

            printStationDao.save(printStation)
            return true
        }
        else {
            return false
        }
    }
}


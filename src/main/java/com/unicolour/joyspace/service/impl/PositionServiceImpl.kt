package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PriceListDao
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.model.PriceListItem
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.PositionService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
open class PositionServiceImpl : PositionService {
    @Autowired
    lateinit var managerService : ManagerService

    @Autowired
    lateinit var managerDao : ManagerDao

    @Autowired
    lateinit var positionDao : PositionDao

    @Autowired
    lateinit var priceListDao : PriceListDao

    @Transactional
    override fun createPosition(name: String, address: String, longitude: Double, latitude: Double, priceListId: Int): Position? {
        val loginManager = managerService.loginManager
        val manager = managerDao.findOne(loginManager.managerId)

        val position = Position()
        position.name = name
        position.address = address
        position.company = manager.company
        position.latitude = latitude
        position.longitude = longitude
        position.priceList =
                if (priceListId <= 0)
                    null
                else
                    priceListDao.findOne(priceListId)

        positionDao.save(position)
        return position
    }

    @Transactional
    override fun updatePosition(id: Int, name: String, address: String, longitude: Double, latitude: Double, priceListId: Int): Boolean {
        val position = positionDao.findOne(id)

        if (position != null) {
            position.name = name
            position.address = address
            position.latitude = latitude
            position.longitude = longitude
            position.priceList =
                    if (priceListId <= 0)
                        null
                    else
                        priceListDao.findOne(priceListId)

            positionDao.save(position)
            return true
        }
        else {
            return false
        }
    }
}
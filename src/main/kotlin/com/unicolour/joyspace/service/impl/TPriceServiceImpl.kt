package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.TPriceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
open class TPriceServiceImpl : TPriceService {

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var tpriceItemDao: TPriceItemDao

    @Autowired
    lateinit var tPriceDao: TPriceDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Transactional
    override
    fun createtp(name: String, begin: Date, expire: Date, product_id: Int,tpriceItem: List<TPriceItem>): Boolean {

        val loginManager = managerService.loginManager
        if (loginManager == null) {

            return false
        }

        val manager = managerDao.findOne(loginManager.managerId)
        val tprice = TPrice()


        val product = productDao.findOne(product_id)

        tprice.name = name
        tprice.begin = begin
        tprice.company = manager.company
        tprice.enabled = true
        tprice.expire = expire
        tprice.product = product

        tPriceDao.save(tprice)

        for (entry in tpriceItem) {

            val price = entry

            if (price != null) {

                val tpriceItem = TPriceItem()

                tpriceItem.price = entry.price
                tpriceItem.maxCount = entry.maxCount
                tpriceItem.minCount = entry.minCount
                tpriceItem.tPrice = tprice

                tpriceItemDao.save(tpriceItem)
            }
        }

        return true
    }

    @Transactional
    override
    fun updatetp(name: String, begin: Date, expire: Date, product_id: Int) {


    }


    @Transactional
    override fun tpriceEnabled(id: Int): Boolean{

        val tprice = tPriceDao.findOne(id)

        if (tprice == null) {

            return false
        }

        if (tprice.enabled == false){

            tprice.enabled = true

        } else {

            tprice.enabled = false
            tPriceDao.save(tprice)
        }

        return true
    }


}
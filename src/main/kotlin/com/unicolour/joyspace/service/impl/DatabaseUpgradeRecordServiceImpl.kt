package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.service.DatabaseUpgradeRecordService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
open class DatabaseUpgradeRecordServiceImpl : DatabaseUpgradeRecordService {
    companion object {
        val logger = LoggerFactory.getLogger(DatabaseUpgradeRecordServiceImpl::class.java)
    }

    @Autowired
    lateinit var dbUpgradeRecordDao: DatabaseUpgradeRecordDao

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Autowired
    lateinit var wxEntTransferRecordDao: WxEntTransferRecordDao

    @Autowired
    lateinit var wxEntTransferRecordItemDao: WxEntTransferRecordItemDao

    @Autowired
    lateinit var productDao: ProductDao

    @Transactional
    override fun upgradeDatabase() {
        if (dbUpgradeRecordDao.exists("InitPrintOrderNewColumns")) {
            val allOrders = printOrderDao.findAll()

            val orders = ArrayList<PrintOrder>()

            val idProductMap = productDao.findAll().map { Pair(it.id, it) }.toMap()
            val idTriMap = wxEntTransferRecordItemDao.findAll().map { Pair(it.printOrderId, it) }.toMap()
            val idTrMap = wxEntTransferRecordDao.findAll().map { Pair(it.id, it) }.toMap()

            for (order in allOrders) {
                if (order.transfered) {
                    val transferRecordItem = idTriMap[order.id]
                    if (transferRecordItem != null) {
                        val transferRecord = idTrMap[transferRecordItem.recordId]
                        if (transferRecord != null) {
                            order.transferTime = transferRecord.transferTime
                            order.transferReceiverName = transferRecord.receiverName
                            order.transferAmount = transferRecordItem.amount
                            order.transferCharge = transferRecordItem.charge
                        }
                    }
                }

                val orderItems = order.printOrderItems
                val productNames = ArrayList<String>()
                val productIdSet = HashSet<Int>()
                var totalPageCount = 0
                for (orderItem in orderItems) {
                    if (!productIdSet.contains(orderItem.productId)) {
                        productIdSet += orderItem.productId
                        val product = idProductMap[orderItem.productId]

                        if (product != null) {
                            productNames += product.name
                            totalPageCount += orderItem.copies
                        }
                    }
                }

                order.productNames = productNames.joinToString(",")
                order.totalPageCount = totalPageCount

                orders += order

                if (orders.size >= 100) {
                    printOrderDao.save(orders)
                    orders.clear()
                }
            }

            if (orders.isNotEmpty()) {
                printOrderDao.save(orders)
                orders.clear()
            }

            dbUpgradeRecordDao.delete("InitPrintOrderNewColumns")
        }
    }
}
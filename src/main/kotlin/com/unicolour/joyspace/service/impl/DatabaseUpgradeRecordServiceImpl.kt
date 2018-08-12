package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.service.DatabaseUpgradeRecordService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
open class DatabaseUpgradeRecordServiceImpl : DatabaseUpgradeRecordService {
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
            for (order in allOrders) {
                if (order.transfered) {
                    val transferRecordItem = wxEntTransferRecordItemDao.findByPrintOrderId(order.id)
                    if (transferRecordItem != null) {
                        val transferRecord = wxEntTransferRecordDao.findOne(transferRecordItem.recordId)
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
                        val product = productDao.findOne(orderItem.productId)

                        productNames += product.name
                        totalPageCount += orderItem.copies
                    }
                }

                order.productNames = productNames.joinToString(",")
                order.totalPageCount = totalPageCount

                printOrderDao.save(order)
            }

            dbUpgradeRecordDao.delete("InitPrintOrderNewColumns")
        }
    }
}
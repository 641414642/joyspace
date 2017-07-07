package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.PrintOrderItem
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.PrintOrderService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
open class PrintOrderServiceImpl : PrintOrderService {
    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Autowired
    lateinit var printOrderItemDao: PrintOrderItemDao

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var productDao: ProductDao

    @Transactional
    override fun createOrder(orderInput: OrderInput): CommonRequestResult {
        val session = userLoginSessionDao.findOne(orderInput.sessionId)

        if (session == null) {
            return CommonRequestResult(1, "用户未登录")
        }

        val printStation = printStationDao.findOne(orderInput.printStationId)

        if (printStation == null) {
            return CommonRequestResult(2, "没有找到指定的自助机")
        }

        val order = PrintOrder()
        order.companyId = printStation.companyId
        order.createTime = Calendar.getInstance()
        order.updateTime = order.createTime
        order.printStationId = orderInput.printStationId
        order.userId = session.userId
        order.state = PrintOrderState.CREATED.value

        printOrderDao.save(order)

        for (orderItem in orderInput.orderItems) {
            val newOrderItem = PrintOrderItem()
            newOrderItem.copies = orderItem.copies
            newOrderItem.printOrder = order
            newOrderItem.product = productDao.findOne(orderItem.productId)
            newOrderItem.userImageFile = userImageFileDao.findOne(orderItem.imageFileId)

            printOrderItemDao.save(newOrderItem)
        }

        return CommonRequestResult()
    }

    override fun getImageFilesDataFetcher(): DataFetcher<Array<UserImageFile>> {
        return DataFetcher { env ->
            val printOrderItem = env.getSource<PrintOrderItem>()
            arrayOf(printOrderItem.userImageFile)
        }
    }

    override fun getPrintOrderDataFetcher(): DataFetcher<PrintOrder> {
        return DataFetcher { env ->
            val printStationId = env.getArgument<Int>("printStationId")
            val idAfter = env.getArgument<Int>("idAfter")

            printOrderDao.findFirstByPrintStationIdAndIdAfter(printStationId, idAfter)
        }
    }
}

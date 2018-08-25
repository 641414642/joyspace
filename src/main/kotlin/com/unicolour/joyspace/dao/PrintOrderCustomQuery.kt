package com.unicolour.joyspace.dao

import com.unicolour.joyspace.dto.PrintOrderStatDTO
import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.util.*

interface PrintOrderCustomQuery {
    fun queryPrintOrders(pageable: Pageable, companyId: Int,
                         startTime: Calendar?, endTime: Calendar?,
                         printStationIds: List<Int>): Page<PrintOrder>

    fun queryPrintOrders(sort: Sort, companyId: Int,
                         startTime: Calendar?, endTime: Calendar?,
                         printStationIds: List<Int>): List<PrintOrder>

    fun printOrderStat(companyId: Int, startTime: Calendar?, endTime: Calendar?,
                       payed: Boolean?, printed: Boolean?, printStationIds: List<Int>): PrintOrderStatDTO

    fun getOldUnClearedPrintOrders(beforeTime: Calendar, limit: Int): List<PrintOrder>
}

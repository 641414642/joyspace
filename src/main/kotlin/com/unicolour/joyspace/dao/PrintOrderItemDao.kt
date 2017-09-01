package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrderItem
import org.springframework.data.repository.CrudRepository

interface PrintOrderItemDao : CrudRepository<PrintOrderItem, Int> {
    fun findByPrintOrderId(printOrderId: Int): List<PrintOrderItem>
}

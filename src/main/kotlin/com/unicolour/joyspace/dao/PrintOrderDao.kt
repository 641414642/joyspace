package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PrintOrderDao : CrudRepository<PrintOrder, Int> {
    fun findFirstByPrintStationIdAndPayedAndImageFileUploadedAndIdAfter(
            printStationId: Int,
            payed: Boolean,
            imageFileUploaded: Boolean,
            idAfter: Int): PrintOrder?

    fun existsByOrderNo(orderNo: String) : Boolean
    fun findByOrderNo(orderNo: String): PrintOrder?
}
package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrinterStatRecord
import org.springframework.data.repository.CrudRepository

interface PrinterStatRecordDao : CrudRepository<PrinterStatRecord, Int>, PrinterStatRecordCustomQuery {
    fun findFirstByPrintStationIdOrderByIdDesc(printStationId: Int): PrinterStatRecord?
}

interface PrinterStatRecordCustomQuery {
    fun getLastMessageRecord(printStationId: Int): PrinterStatRecord?
}


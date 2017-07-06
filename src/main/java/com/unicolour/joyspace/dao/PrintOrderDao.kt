package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PrintOrderDao : CrudRepository<PrintOrder, Int> {
    fun findFirstByPrintStationIdAndUpdateTimeAfter(printStationId: Int, updateTimeAfter: Calendar): PrintOrder
}
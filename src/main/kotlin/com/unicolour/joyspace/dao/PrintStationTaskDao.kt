package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStationTask
import org.springframework.data.repository.CrudRepository

interface PrintStationTaskDao : CrudRepository<PrintStationTask, Int> {
    fun findByPrintStationIdAndIdGreaterThanAndFetchedIsFalse(printStationId: Int, taskIdAfter: Int) : List<PrintStationTask>
    fun existsByPrintStationIdAndParamAndFetchedIsFalse(printStationId: Int, param: String): Boolean
}

package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PrintStationCustomQuery {
    fun queryPrintStations(pageable: Pageable, companyId: Int, positionId: Int, printStationId: Int, name: String, printerModel: String): Page<PrintStation>
    fun queryPrintStations(companyId: Int, positionId: Int, printStationId: Int, name: String, printerModel: String): List<PrintStation>
}

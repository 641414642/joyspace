package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.StationType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PrintStationCustomQuery {
    fun queryPrintStations(pageable: Pageable, companyId: Int, positionId: Int, printStationId: Int, name: String, stationType: StationType?, printerModel: String, onlineOnly: Boolean): Page<PrintStation>
    fun queryPrintStations(companyId: Int, positionId: Int, printStationId: Int, name: String, stationType: StationType?, printerModel: String, onlineOnly: Boolean): List<PrintStation>
}

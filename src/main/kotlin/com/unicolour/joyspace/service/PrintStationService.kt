package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.PrintStationDetailDTO
import com.unicolour.joyspace.dto.PrintStationFindResult
import com.unicolour.joyspace.model.PrintStation
import graphql.schema.DataFetcher

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation): Map<Int, Int>
    fun createPrintStation(wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation?
    fun updatePrintStation(id: Int, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): Boolean

    val printStationDataFetcher: DataFetcher<PrintStation>
    val nearestDataFetcher: DataFetcher<PrintStationFindResult>
    val byCityDataFetcher: DataFetcher<PrintStationFindResult>
    val byDistanceDataFetcher: DataFetcher<PrintStationFindResult>

    fun getDataFetcher(fieldName:String): DataFetcher<Any>
}
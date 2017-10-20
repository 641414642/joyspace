package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.PrintStation
import graphql.schema.DataFetcher

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation): Map<Int, Int>
    fun createPrintStation(password: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation?
    fun updatePrintStation(id: Int, password: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): Boolean

    val loginDataFetcher: DataFetcher<PrintStationLoginResult>
    val printStationDataFetcher: DataFetcher<PrintStation>
    val nearestDataFetcher: DataFetcher<PrintStationFindResultSingle>
    val byCityDataFetcher: DataFetcher<PrintStationFindResult>
    val byDistanceDataFetcher: DataFetcher<PrintStationFindResult>

    fun getDataFetcher(fieldName:String): DataFetcher<Any>
}
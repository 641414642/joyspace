package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.model.PrintStationLoginSession
import graphql.schema.DataFetcher
import javax.transaction.Transactional

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation): Map<Int, Int>
    fun createPrintStation(baseUrl: String, password: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation?
    fun updatePrintStation(id: Int, baseUrl: String, password: String, positionId: Int, selectedProductIds: Set<Int>): Boolean

    val loginDataFetcher: DataFetcher<PrintStationLoginResult>
    val printStationDataFetcher: DataFetcher<PrintStation>
    val nearestDataFetcher: DataFetcher<PrintStationFindResultSingle>
    val byCityDataFetcher: DataFetcher<PrintStationFindResult>
    val byDistanceDataFetcher: DataFetcher<PrintStationFindResult>

    fun getDataFetcher(fieldName:String): DataFetcher<Any>
    fun getPrintStationLoginSession(sessionId: String): PrintStationLoginSession?
}
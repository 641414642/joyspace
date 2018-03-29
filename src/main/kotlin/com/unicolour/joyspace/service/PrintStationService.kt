package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.PrintStationFindResult
import com.unicolour.joyspace.dto.PrintStationFindResultSingle
import com.unicolour.joyspace.dto.PrintStationLoginResult
import com.unicolour.joyspace.model.*
import graphql.schema.DataFetcher

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation): Map<Int, Int>
    fun createPrintStation(companyId: Int, printStationName: String, printStationPassword: String, positionId: Int, transferProportion:Int, printerType:String, adSetId: Int, selectedProductIds: Set<Int>): PrintStation?
    fun updatePrintStation(id: Int, companyId: Int, printStationName: String, printStationPassword: String, positionId: Int, transferProportion:Int, printerType:String, adSetId: Int, selectedProductIds: Set<Int>): Boolean

    fun activatePrintStation(code: String, name:String, password: String, positionId: Int, selectedProductIds: Set<Int>)

    val loginDataFetcher: DataFetcher<PrintStationLoginResult>
    val printStationDataFetcher: DataFetcher<PrintStation>
    val nearestDataFetcher: DataFetcher<PrintStationFindResultSingle>
    val byCityDataFetcher: DataFetcher<PrintStationFindResult>

    val byDistanceDataFetcher: DataFetcher<PrintStationFindResult>
    val newAdSetDataFetcher: DataFetcher<AdSet?>

    val currentSoftwareVersionDataFetcher: DataFetcher<Int>
    fun getDataFetcher(fieldName:String): DataFetcher<Any>
    fun getPrintStationLoginSession(sessionId: String): PrintStationLoginSession?

    fun getPrintStationUrl(printStationId: Int): String

    fun createPrintStationTask(printStationId: Int, type: PrintStationTaskType, param: String)
    fun getUnFetchedPrintStationTasks(printStationSessionId: String, taskIdAfter: Int): List<PrintStationTask>
    fun printStationTaskFetched(printStationSessionId: String, taskId: Int): Boolean
}
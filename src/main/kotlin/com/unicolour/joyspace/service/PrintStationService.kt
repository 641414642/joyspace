package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.*
import graphql.schema.DataFetcher

interface PrintStationService {
    fun login(printStationId: Int, password: String, version: Int?, uuid: String): PrintStationLoginResult
    fun loginWithKey(printStationId: Int, signStr: String, version: Int?): PrintStationLoginResult
    fun initPublicKey(printStationId: Int, uuid: String, pubKeyStr: String): Int

    fun getPriceMap(printStation: PrintStation): Map<Int, Int>

    fun updatePrintStation(id: Int, printStationName: String, positionId: Int, transferProportion: Int,
                           printerType: String, adSetId: Int, selectedProductIds: Set<Int>): Boolean

    fun updatePrintStationPassword(id: Int, printStationPassword: String): Boolean
    fun updatePrintStationStatus(printStationSessionId: String, status: PrintStationStatus, additionalInfo: String): Boolean
    fun activatePrintStation(manager: Manager?, code: String, name:String, password: String,
                             positionId: Int, selectedProductIds: Set<Int>, uuid: String)
    val loginDataFetcher: DataFetcher<PrintStationLoginResultOld>

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
    fun printStationTaskFetched(printStationId: Int, printOrderId: Int)
    fun uploadLog(printStationSessionId: String, fileName: String, logText: String): Boolean
    fun addUploadLogFileTask(printStationId: Int, filterStr: String): Boolean
    fun orderReprintTaskExists(printStationId: Int, orderId: Int): Boolean
    fun getPrintStationUpdateAndAdSet(sessionId: String, currentVersion: Int, currentAdSetId: Int, currentAdSetTimeStr: String): UpdateAndAdSetDTO
    fun getHomeInitInfo(userName: String, password: String, printStationId: Int): HomeInitInfoDTO
    fun initHome(input: HomeInitInput): ResultCode

    fun recordPrinterStat(sessionId: String, printerSn: String, printerType: String, printerName: String, mediaCounter: Int, errorCode: Int): Boolean
    fun getPrintStationQrCodeUrl(printStationId: Int): String
}
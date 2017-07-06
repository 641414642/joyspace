package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStation
import graphql.schema.DataFetcher

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation?): Map<Int, Int>
    fun createPrintStation(sn: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): PrintStation?
    fun updatePrintStation(id: Int, sn: String, wxQrCode: String, positionId: Int, selectedProductIds: Set<Int>): Boolean

    fun getPrintStationDataFetcher(): DataFetcher<PrintStation>
    fun getDataFetchers(): Map<String, DataFetcher<Any>>
}
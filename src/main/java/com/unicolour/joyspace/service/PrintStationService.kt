package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStation
import javax.transaction.Transactional

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation?): Map<Int, Int>
    fun createPrintStation(sn: String, wxQrCode: String, positionId: Int): PrintStation?
    fun updatePrintStation(id: Int, sn: String, wxQrCode: String, positionId: Int): Boolean
}
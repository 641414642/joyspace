package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStation

interface PrintStationService {
    fun getPriceMap(printStation: PrintStation?): Map<Int, Int>
}
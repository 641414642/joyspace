package com.unicolour.joyspace.service

interface PrintStationActivationCodeService {
    fun batchCreateActivationCodes(printStationIdStart: Int, quantity: Int,
                                   printerType: String, proportion: Int, adSetId: Int): Boolean
}
package com.unicolour.joyspace.dto

class HomeInitInfoDTO(
        var result: Int = ResultCode.INVALID_ACTIVATION_CODE.value,
        var printerType: String = ""
)

class HomeInitInput(
        var username: String = "",
        var password: String = "",
        var printStationId: Int = 0,
        var publicKey: String = "",
        var uuid: String = ""
)
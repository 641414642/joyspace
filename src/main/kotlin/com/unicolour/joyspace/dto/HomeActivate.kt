package com.unicolour.joyspace.dto

class HomeActivateInfoDTO(
        var result: Int = ResultCode.INVALID_ACTIVATION_CODE.value,
        var printerType: String = "",
        var printStationName: String = "",
        var printStationId: Int = 0,
        var positions: List<PositionIDAndName> = emptyList(),
        var products: List<ProductIDNameAndType> = emptyList()
)

class PositionIDAndName(
        var id: Int = 0,
        var name: String = "",
        var selected: Boolean = false
)

class ProductIDNameAndType(
        var id: Int = 0,
        var name: String = "",
        var type: Int = 0,
        var selected: Boolean = false
)

class HomeActivateInput(
        var username: String = "",
        var password: String = "",
        var printStationId: Int = 0,
        var activateCode: String = "",
        var printStationName: String = "",
        var positionId: Int = 0,
        var productIds: List<Int> = emptyList(),
        var publicKey: String = "",
        var uuid: String = ""
)
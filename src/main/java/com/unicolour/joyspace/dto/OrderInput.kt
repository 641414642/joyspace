package com.unicolour.joyspace.dto

data class OrderInput(
        var sessionId: String = "",
        var printStationId: Int = 0,
        var orderItems: Array<OrderItemInput> = emptyArray()
)

data class OrderItemInput(
        var copies: Int = 1,
        var imageFileId: Int = 0,
        var productId: Int = 0
)
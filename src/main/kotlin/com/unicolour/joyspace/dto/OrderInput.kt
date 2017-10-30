package com.unicolour.joyspace.dto

class OrderInput(
        var sessionId: String = "",
        var printStationId: Int = 0,
        var orderItems: Array<OrderItemInput> = emptyArray()
)

class OrderItemInput(
        var copies: Int = 1,
        var productId: Int = 0,
        var images: List<ImageParam>? = null
)
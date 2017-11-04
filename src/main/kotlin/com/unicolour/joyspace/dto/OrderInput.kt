package com.unicolour.joyspace.dto

class OrderInput(
        var sessionId: String = "",
        var printStationId: Int = 0,
        var orderItems: Array<OrderItemInput> = emptyArray(),
        var coupon: String? = ""
)

class OrderItemInput(
        var copies: Int = 1,
        var productId: Int = 0,           //产品id
        var productVersion: String = "",  //产品版本号
        var images: List<ImageParam>? = null
)
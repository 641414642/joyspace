package com.unicolour.joyspace.dto


class OrderInput(
        var sessionId: String = "",
        var printStationId: Int = 0,
        var orderItems: Array<OrderItemInput> = emptyArray(),
        var couponId: Int = 0,
        var province: String? = null,
        var city: String? = null,
        var area: String? = null,
        var address: String? = null,
        var phoneNum: String? = null,
        var name: String? = null,
        var printType: Int = 0
        //var orderProImgs: Array<OrderProImgInput> = emptyArray()
)

//class OrderProImgInput(
//        var productId: Int? = null,
//        var image: MultipartFile? = null
//)

class OrderItemInput(
        var copies: Int = 1,
        var productId: Int = 0,           //产品id
        var productVersion: String = "",  //产品版本号
        var area: Double = 0.0,
        var piece: Int = 0,
        var images: List<ImageParam>? = null
)

class OrderImgProcessParam(
        var x: Double = 0.0,
        var y: Double = 0.0,
        var scale: Double = 0.0,
        var rotate: Double = 0.0,
        var dpi: Int = 0
)

class OrderCancelInput(
        var sessionId: String = "",
        var orderId: Int = 0)

class OrderPayInput(
        var sessionId: String = "",
        var orderId: Int = 0
)

class CouponInput(
        var sessionId: String = "",
        var couponCode: String = ""
)

class CouponAutoInput(
        var sessionId: String = "",
        var printStationId: Int = 0
)

class AddressInput(
        var sessionId: String? = null,
        var province: String? = null,
        var city: String? = null,
        var area: String? = null,
        var address: String? = null,
        var phoneNum: String? = null,
        var name: String? = null,
        var default: Int? = null,
        var id: Int? = null
)
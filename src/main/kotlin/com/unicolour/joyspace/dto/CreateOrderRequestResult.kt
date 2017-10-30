package com.unicolour.joyspace.dto

class CreateOrderRequestResult(
        var wxPayParams: WxPayParams?,
        var orderItems: List<OrderItemRet>?,
        errcode: Int = 0,
        errmsg: String? = null)
    : CommonRequestResult(errcode, errmsg)
{
    constructor(errcode: Int, errmsg: String?) : this(null, null, errcode, errmsg)
}

class OrderItemRet(
        var id: Int = 0,
        var productId: Int = 0
)

class UploadOrderImageResult(
        var allImagesUploaded: Boolean,
        errcode: Int = 0,
        errmsg: String? = null
) : CommonRequestResult(errcode, errmsg)

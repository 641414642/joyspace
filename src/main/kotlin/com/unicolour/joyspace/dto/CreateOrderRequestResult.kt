package com.unicolour.joyspace.dto

class CheckOrderRequestResult(
        val canPrint: Boolean,
        val totalFee: Int,
        val discount: Int,
        errcode: Int = 0,
        errmsg: String? = null
) : CommonRequestResult(errcode, errmsg)

class CreateOrderRequestResult(
        val orderId: Int,
        val orderNo: String?,
        val wxPayParams: WxPayParams?,
        val orderItems: List<OrderItemRet>?,
        val totalFee: Int,
        val discount: Int,
        errcode: Int = 0,
        errmsg: String? = null)
    : CommonRequestResult(errcode, errmsg)
{
    constructor(errcode: Int, errmsg: String?) : this(0, null,null, null, 0, 0, errcode, errmsg)
}

class OrderItemRet(
        var id: Int = 0,
        var productId: Int = 0,
        var copies: Int = 0
)

class UploadOrderImageResult(
        var allImagesUploaded: Boolean,
        errcode: Int = 0,
        errmsg: String? = null
) : CommonRequestResult(errcode, errmsg)

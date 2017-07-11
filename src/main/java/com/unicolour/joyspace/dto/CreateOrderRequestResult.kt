package com.unicolour.joyspace.dto

class CreateOrderRequestResult(var wxPayParams: WxPayParams?, errcode: Int = 0, errmsg: String? = null)
    : CommonRequestResult(errcode, errmsg) {
    constructor(errcode: Int, errmsg: String?) : this(null, errcode, errmsg)
}
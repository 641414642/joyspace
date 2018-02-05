package com.unicolour.joyspace.exception

import com.unicolour.joyspace.dto.ResultCode

class ProcessException(var errcode: Int, errMessage: String) : RuntimeException(errMessage) {
    constructor(resultCode: ResultCode) : this(resultCode.value, resultCode.desc)
    constructor(resultCode: ResultCode, errMessage: String) : this(resultCode.value, errMessage)
}
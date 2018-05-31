package com.unicolour.joyspace.dto.common

import com.unicolour.joyspace.dto.ResultCode

/**
 * 接口返回
 */
class RestResponse() {
    /** 业务状态码  */
    var state: Int? = null
    /** 数据  */
    var result: Any? = null
    /** 消息  */
    var msg: String? = null


    constructor(state: Int?, result: Any?, msg: String?) : this() {
        this.state = state
        this.result = result
        this.msg = msg
    }

    companion object {


        fun ok(): RestResponse {
            return RestResponse(0, null, "ok")
        }

        fun ok(data: Any): RestResponse {
            return RestResponse(0, data, "ok")
        }

        fun error(resultCode: ResultCode, language: String = "zh"): RestResponse {
            return RestResponse(resultCode.value, null, if (language != "en") resultCode.desc else resultCode.descEn)
        }

        fun error(resultCode: ResultCode, data: Any, language: String = "zh"): RestResponse {
            return RestResponse(resultCode.value, data, if (language != "en") resultCode.desc else resultCode.descEn)
        }
    }
}

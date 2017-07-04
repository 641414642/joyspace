package com.unicolour.joyspace.dto

data class WxLoginResult(
        val errcode: Int? = 0,
        val errmsg: String? = null,
        val sessionId: String? = null
)
package com.unicolour.joyspace.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class WxGetAccessTokenResult(
        var errcode: Int = 0,
        var errmsg: String = "",

        var access_token: String = "",
        var expires_in: Int = 0,
        var refresh_token: String = "",
        var openid: String = "",
        var scope: String = ""
)


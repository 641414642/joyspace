package com.unicolour.joyspace.util

import javax.servlet.http.HttpServletRequest

fun getBaseUrl(request: HttpServletRequest) : String {
    val host: String? = request.getHeader("host");
    val protocol: String? = request.getHeader("x-forwarded-proto");

    var baseUrl: String = "http://localhost:6060"
    if (host != null && protocol != null) {
        baseUrl = protocol + "://" + host
    }

    return baseUrl
}
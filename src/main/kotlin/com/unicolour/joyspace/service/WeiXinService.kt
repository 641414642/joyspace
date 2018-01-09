package com.unicolour.joyspace.service

interface WeiXinService {
    val accessToken: String?
    fun createWxQrCode()
}


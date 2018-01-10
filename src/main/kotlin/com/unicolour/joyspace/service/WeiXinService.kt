package com.unicolour.joyspace.service

interface WeiXinService {
    fun createWxQrCode(scene: String, page: String, width: Int = 300): String
}


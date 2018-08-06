package com.unicolour.joyspace.service

interface WeiXinService {
    //fun createWxQrCode(scene: String, page: String, width: Int = 300): String

    /** 群发文本信息 */
    fun sendTextMessage(message: String, openIdList: List<String>, wxMpAccountId: Int, preview: Boolean)
}


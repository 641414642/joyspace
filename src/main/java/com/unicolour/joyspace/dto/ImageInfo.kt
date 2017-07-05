package com.unicolour.joyspace.dto

/** 上传图片的返回信息 */
data class ImageInfo(
        var imageId: Int = 0,
        var width: Int = 0,
        var height: Int = 0,
        var thumbUrl: String = "",
        var thumbWidth: Int = 0,
        var thumbHeight: Int = 0,
        var errcode: Int = 0,
        val errmsg: String? = null
)

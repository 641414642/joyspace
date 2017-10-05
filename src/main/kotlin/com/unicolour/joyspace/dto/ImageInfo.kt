package com.unicolour.joyspace.dto

/** 上传图片的返回信息 */
class ImageInfo(
        var errcode: Int = 0,
        var errmsg: String? = null,

        var imageId: Int = 0,

        var width: Int = 0,
        var height: Int = 0,

        var url: String = ""
)

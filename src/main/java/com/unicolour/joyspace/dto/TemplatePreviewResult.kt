package com.unicolour.joyspace.dto

class TemplatePreviewResult(
        var errcode: Int = 0,
        var errmsg: String? = null,

        val svgUrl: String? = null,
        val jpgUrl: String? = null
)
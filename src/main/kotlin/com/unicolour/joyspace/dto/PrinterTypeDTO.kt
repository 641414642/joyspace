package com.unicolour.joyspace.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


/** 打印机类型 */
@JsonIgnoreProperties(ignoreUnknown = true)
class PrinterTypeDTO(
        var name: String = "",
        var displayName: String = "",
        var resolution: Int = 0,         //打印分辨率
        var rollPaper: Boolean = false   //是否是卷纸
)


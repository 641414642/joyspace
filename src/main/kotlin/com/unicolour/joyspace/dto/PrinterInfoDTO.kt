package com.unicolour.joyspace.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** 打印机信息 */
@JsonIgnoreProperties(ignoreUnknown = true)
class PrinterInfoDTO(
        var name: String = "",    //打印机名称
        var model: String = "",   //打印机型号 (Driver Name)
        var paperSizes: List<PaperSizeDTO> = emptyList(),           //支持的纸张尺寸列表
        var rollPaper: Boolean = false   //是否是卷纸
)

@JsonIgnoreProperties(ignoreUnknown = true)
class PaperSizeDTO(
        var paperId: Short = 0,
        var paperWidth: Double = 0.0,
        var paperLength: Double = 0.0,
        var current: Boolean = false
)

package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.PrintOrder

class PrintOrderResult (
        //结果代码 0: 成功
        var result: Int = 0,

        //结果的描述
        var description: String? = null,

        //自助机列表
        var printOrder: PrintOrder? = null
)

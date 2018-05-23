package com.unicolour.joyspace.dto

data class PrintStationLoginResultOld(
        //结果代码
        //0: 成功
        //1: 没有找到id对应的自助机
        //2: 密码错误
        //3: 其他自助机已登录
        var result: Int = 0,

        var sessionId: String? = null,

        //打印机型号
        var printerType: String = "",

        //打印分辨率
        var resolution: Int = 0
)

data class PrintStationLoginResult(
        //结果代码
        //0: 成功
        //1: 没有找到id对应的自助机
        //2: 密码错误
        //3: 其他自助机已登录
        var result: Int = 0,

        var sessionId: String? = null,

        //打印机型号
        var printerType: PrinterTypeDTO = PrinterTypeDTO()
)
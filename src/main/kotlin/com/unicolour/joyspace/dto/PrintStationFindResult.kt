package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.PrintStation

//查找自助机的结果
class PrintStationFindResult (
        //结果代码 0: 成功
        var result: Int = 0,

        //结果的描述
        var description: String? = null,

        //自助机列表
        var printStations: List<PrintStation> = emptyList()
)
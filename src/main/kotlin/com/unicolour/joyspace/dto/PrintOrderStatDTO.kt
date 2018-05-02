package com.unicolour.joyspace.dto

class PrintOrderStatDTO(
        var orderCount: Int = 0,   //订单数量
        var printPageCount: Int = 0,    //打印页数
        var totalAmount: Int = 0,       //总金额（分）
        var totalDiscount: Int = 0      //总折扣金额（分）
)
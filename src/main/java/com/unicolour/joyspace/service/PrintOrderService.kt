package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.OrderInput

interface PrintOrderService {
    fun createOrder(orderInput: OrderInput): CommonRequestResult
}
package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.service.PrintOrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class ApiPrintOrderController {
    @Autowired
    lateinit var printOrderService: PrintOrderService

    @RequestMapping("/api/order", method = arrayOf(RequestMethod.POST))
    fun uploadImage(@RequestBody orderInput: OrderInput) : ResponseEntity<CommonRequestResult> {
        val result = printOrderService.createOrder(orderInput)
        return ResponseEntity.ok(result)
    }
}


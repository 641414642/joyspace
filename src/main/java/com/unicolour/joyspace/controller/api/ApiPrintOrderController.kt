package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.CreateOrderRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.service.PrintOrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest


@RestController
class ApiPrintOrderController {
    private val logger = LoggerFactory.getLogger(ApiPrintOrderController::class.java)

    @Autowired
    lateinit var printOrderService: PrintOrderService

    @RequestMapping("/api/order", method = arrayOf(RequestMethod.POST))
    fun uploadImage(@RequestBody orderInput: OrderInput) : ResponseEntity<CreateOrderRequestResult> {
        try {
            val order = printOrderService.createOrder(orderInput)
            try {
                val params = printOrderService.startPayment(order.id)
                logger.info(params.toString())
                logger.error(params.toString())

                return ResponseEntity.ok(CreateOrderRequestResult(params))
            }
            catch (ex: Exception) {
                ex.printStackTrace()
                return ResponseEntity.ok(CreateOrderRequestResult(null, 0, null))
            }
        } catch(e: ProcessException) {
            e.printStackTrace()
            return ResponseEntity.ok(CreateOrderRequestResult(e.errcode, e.message))
        }
    }

    @RequestMapping("/wxpay/notify",
            method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_XML_VALUE))
    fun wxPayNotify(request: HttpServletRequest) : ResponseEntity<String> {
        InputStreamReader(request.inputStream, StandardCharsets.UTF_8).use {
            reader ->
                val errmsg = printOrderService.processWxPayNotify(reader.readText())
                val retCode = if (errmsg == null) "SUCCESS" else "FAIL"
                val retMsg =  if (errmsg == null) "OK" else errmsg

                return ResponseEntity.ok("<xml>" +
                    "<return_code><![CDATA[$retCode]]></return_code>" +
                    "<return_msg><![CDATA[$retMsg]]></return_msg>" +
                    "</xml>")
        }
    }
}


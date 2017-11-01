package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.service.PrintOrderService
import com.unicolour.joyspace.util.getBaseUrl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest


@RestController
class ApiPrintOrderController {
    private val logger = LoggerFactory.getLogger(ApiPrintOrderController::class.java)

    @Autowired
    lateinit var printOrderService: PrintOrderService

    @RequestMapping("/api/order/check", method = arrayOf(RequestMethod.POST))
    fun checkOrder(request: HttpServletRequest, @RequestBody orderInput: OrderInput) :
            ResponseEntity<CheckOrderRequestResult> {
        try {
            //XXX
            val canPrint = orderInput.orderItems.filter { it.copies > 10 }.isEmpty()
            val ret = printOrderService.calculateOrderFee(orderInput)
            val totalFee = ret.first
            val discount = ret.second
            return ResponseEntity.ok(CheckOrderRequestResult(canPrint, totalFee, discount))
        } catch(e: ProcessException) {
            e.printStackTrace()
            return ResponseEntity.ok(CheckOrderRequestResult(false, 0, 0, e.errcode, e.message))
        }  catch (ex: Exception) {
            ex.printStackTrace()
            return ResponseEntity.ok(CheckOrderRequestResult(false, 0, 0,1, ex.message))
        }
    }

    @RequestMapping("/api/order/create", method = arrayOf(RequestMethod.POST))
    fun createOrder(request: HttpServletRequest, @RequestBody orderInput: OrderInput) :
            ResponseEntity<CreateOrderRequestResult> {
        try {
            val baseUrl = getBaseUrl(request)
            val order = printOrderService.createOrder(orderInput)
            val params = printOrderService.startPayment(order.id, baseUrl)
            val orderItems = order.printOrderItems.map { OrderItemRet(it.id, it.productId) }
            return ResponseEntity.ok(CreateOrderRequestResult(params, orderItems))
        } catch(e: ProcessException) {
            e.printStackTrace()
            return ResponseEntity.ok(CreateOrderRequestResult(e.errcode, e.message))
        }  catch (ex: Exception) {
            ex.printStackTrace()
            return ResponseEntity.ok(CreateOrderRequestResult(1, ex.message))
        }
    }

    //上传订单图片文件
    @RequestMapping("/api/order/image", method = arrayOf(RequestMethod.POST))
    fun uploadOrderItemImage(request: HttpServletRequest,
                    @RequestParam("sessionId") sessionId: String,
                    @RequestParam("orderItemId") orderItemId: Int,
                    @RequestParam("name") name: String,
                    @RequestParam("initialRotate") initialRotate: Int,
                    @RequestParam("scale") scale: Double,
                    @RequestParam("rotate") rotate: Double,
                    @RequestParam("horTranslate") horTranslate: Double,
                    @RequestParam("verTranslate") verTranslate: Double,
                    @RequestParam("brightness") brightness: Double,
                    @RequestParam("saturate") saturate: Double,
                    @RequestParam("effect") effect: String,
                    @RequestParam("image") imgFile: MultipartFile?) : ResponseEntity<UploadOrderImageResult> {
        val imageProcessParam = ImageProcessParams(
                initialRotate,
                scale,
                rotate,
                horTranslate,
                verTranslate,
                brightness,
                saturate,
                effect
        )

        val allUploaded = printOrderService.uploadOrderItemImage(sessionId, orderItemId, name, imageProcessParam, imgFile)
        return ResponseEntity.ok(UploadOrderImageResult(allUploaded))
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


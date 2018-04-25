package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.service.GraphQLService
import com.unicolour.joyspace.service.PrintOrderService
import graphql.GraphQL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest


@RestController
class ApiPrintOrderController {
    private val logger = LoggerFactory.getLogger(ApiPrintOrderController::class.java)

    @Autowired
    lateinit var graphQLService: GraphQLService

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
    fun createOrder(@RequestBody orderInput: OrderInput) :
            ResponseEntity<CreateOrderRequestResult> {
        try {
            val order = printOrderService.createOrder(orderInput)
            val params = printOrderService.startPayment(order.id)
            //val params: WxPayParams? = null
            val orderItems = order.printOrderItems.map { OrderItemRet(it.id, it.productId) }
            return ResponseEntity.ok(CreateOrderRequestResult(order.id, order.orderNo, params, orderItems, order.totalFee, order.discount))
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
                    @RequestParam("printReady", required = false, defaultValue = "false") printReady: Boolean,   //是否是可以直接打印的图片
                    @RequestParam("initialRotate", required = false, defaultValue = "0") initialRotate: Int,
                    @RequestParam("scale", required = false, defaultValue = "0") scale: Double,
                    @RequestParam("rotate", required = false, defaultValue = "0") rotate: Double,
                    @RequestParam("horTranslate", required = false, defaultValue = "0") horTranslate: String,
                    @RequestParam("verTranslate", required = false, defaultValue = "0") verTranslate: String,
                    @RequestParam("brightness", required = false, defaultValue = "1") brightness: Double,
                    @RequestParam("saturate", required = false, defaultValue = "1") saturate: Double,
                    @RequestParam("effect", required = false, defaultValue = "none") effect: String,
                    @RequestParam("image") imgFile: MultipartFile?) : ResponseEntity<UploadOrderImageResult> {
        val imageProcessParam =
                if (printReady) {
                    null
                } else {
                    ImageProcessParams(
                            initialRotate,
                            scale,
                            rotate,
                            horTranslate,
                            verTranslate,
                            brightness,
                            saturate,
                            effect
                    )
                }

        val allUploaded = printOrderService.uploadOrderItemImage(sessionId, orderItemId, name, imageProcessParam, imgFile)
        return ResponseEntity.ok(UploadOrderImageResult(allUploaded))
    }

    @RequestMapping("/api/order/status", method = arrayOf(RequestMethod.GET))
    fun printOrderStatus(@RequestParam("sessionId") sessionId: String,
                         @RequestParam("printOrderId") printOrderId: Int) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
                """
query {
	printOrder(sessionId:"${sessionId}", printOrderId:$printOrderId) {
        errcode:result
        errmsg:description
        printOrder {
            orderItems:printOrderItems {
                images:orderImages {
                    status
                }
            }
        }
	}
}
"""
        val context = HashMap<String, Any>()

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["printOrder"]
    }
}


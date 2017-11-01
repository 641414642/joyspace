package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.ImageProcessParams
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.dto.WxPayParams
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface PrintOrderService {
    val imageFilesDataFetcher: DataFetcher<Array<UserImageFile>>
    val printOrderDataFetcher: DataFetcher<PrintOrder?>
    val printerOrderDownloadedDataFetcher: DataFetcher<GraphQLRequestResult>
    val printerOrderPrintedDataFetcher: DataFetcher<GraphQLRequestResult>

    fun createOrder(orderInput: OrderInput): PrintOrder
    fun startPayment(orderId: Int, baseUrl:String): WxPayParams
    fun processWxPayNotify(requestBodyStr: String): String?

    //所有图片都上传完成后返回true
    fun uploadOrderItemImage(sessionId: String, orderItemId: Int, name:String, imageProcessParam: ImageProcessParams, imgFile: MultipartFile?): Boolean

    //计算订单价格
    fun calculateOrderFee(orderInput: OrderInput): Pair<Int, Int>
}
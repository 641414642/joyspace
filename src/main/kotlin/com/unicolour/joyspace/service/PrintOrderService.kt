package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface PrintOrderService {
    val imageFilesDataFetcher: DataFetcher<Array<UserImageFile>>
    val printStationPrintOrdersDataFetcher: DataFetcher<List<PrintOrder>>
    val printOrderDataFetcher: DataFetcher<PrintOrderResult>
    val printerOrderDownloadedDataFetcher: DataFetcher<GraphQLRequestResult>
    val printerOrderPrintedDataFetcher: DataFetcher<GraphQLRequestResult>
    val updatePrintOrderImageStatusDataFetcher: DataFetcher<GraphQLRequestResult>
    val wxUserNickNameDataFetcher: DataFetcher<String?>

    fun createOrder(orderInput: OrderInput): PrintOrder
    fun startPayment(orderId: Int): WxPayParams

    fun processWxPayNotify(requestBodyStr: String): String?

    //所有图片都上传完成后返回true
    fun uploadOrderItemImage(sessionId: String, orderItemId: Int, name:String, imageProcessParam: ImageProcessParams?, imgFile: MultipartFile?): Boolean

    //计算订单价格
    fun calculateOrderFee(orderInput: OrderInput): Pair<Int, Int>

    fun getPrintOrderDTO(printOrderId: Int): PrintOrderDTO?

    fun addReprintOrderTask(printOrderId: Int, printStationId: Int)

    //订单统计
    fun printOrderStat(startTime: Calendar, endTime: Calendar): PrintOrderStatDTO
}
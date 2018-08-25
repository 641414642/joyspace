package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher
import org.springframework.data.domain.Page
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
    fun uploadOrderItemImage(sessionId: String, orderItemId: Int, name: String, imageProcessParam: ImageProcessParams?, imgFile: MultipartFile?): Boolean
    //上传单张合成订单图片
    fun uploadOrderImage(filterImageId: String,sessionId: String, orderItemId: Int,name: String, imgFile: MultipartFile?, x: Double, y: Double, scale: Double, rotate: Double,totalCount: Int): Boolean


    //计算订单价格
    fun calculateOrderFee(orderInput: OrderInput): Pair<Int, Int>

    fun getPrintOrderDTO(printOrderId: Int): PrintOrderDTO?

    fun addReprintOrderTask(printOrderId: Int, printStationId: Int)

    //订单统计
    fun printOrderStat(companyId: Int, startTime: Calendar, endTime: Calendar, positionId: Int, printStationId: Int): PrintOrderStatDTO

    //订单查询
    fun queryPrinterOrders(pageNo: Int, pageSize: Int,
                           companyId: Int,
                           startTime: Calendar?, endTime: Calendar?,
                           positionId: Int, printStationId: Int,
                           order: String): Page<PrintOrder>
    //订单查询
    fun queryPrinterOrders(companyId: Int,
                           startTime: Calendar?, endTime: Calendar?,
                           positionId: Int, printStationId: Int,
                           order: String): List<PrintOrder>

    //更新订单图片状态
    fun updatePrintOrderImageStatus(sessionId: String, printOrderImageId: Int, status: Int): ResultCode

    //更新订单状态
    fun updatePrintOrderStatus(sessionId: String, printOrderId: Int, status: String): ResultCode
}
package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.dto.WxPayParams
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher

interface PrintOrderService {
    val imageFilesDataFetcher: DataFetcher<Array<UserImageFile>>
    val printOrderDataFetcher: DataFetcher<PrintOrder?>
    fun startPayment(orderId: Int, baseUrl:String): WxPayParams
    fun createOrder(orderInput: OrderInput): PrintOrder
    fun getUpdateOrderStateDataFetcher(state: PrintOrderState): DataFetcher<GraphQLRequestResult>
    fun processWxPayNotify(requestBodyStr: String): String?
}
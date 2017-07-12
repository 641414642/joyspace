package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.dto.WxPayParams
import com.unicolour.joyspace.dto.WxUnifyOrderResult
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher

interface PrintOrderService {
    fun createOrder(orderInput: OrderInput): PrintOrder
    fun getImageFilesDataFetcher(): DataFetcher<Array<UserImageFile>>
    fun getPrintOrderDataFetcher(): DataFetcher<PrintOrder?>
    fun startPayment(orderId: Int): WxPayParams
    fun getUpdateOrderStateDataFetcher(state: PrintOrderState): DataFetcher<GraphQLRequestResult>
    fun processWxPayNotify(requestBodyStr: String): String?
}
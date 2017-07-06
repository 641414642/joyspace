package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.OrderInput
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher

interface PrintOrderService {
    fun createOrder(orderInput: OrderInput): CommonRequestResult
    fun getImageFilesDataFetcher(): DataFetcher<Array<UserImageFile>>
    fun getPrintOrderDataFetcher(): DataFetcher<PrintOrder>
}
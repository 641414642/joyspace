package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStationProduct

interface ProductService {
    fun getProductsOfPrintStation(printStationId: Int) : List<PrintStationProduct>
}
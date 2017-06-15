package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.service.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProductServiceImpl : ProductService {
    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    override fun getProductsOfPrintStation(printStationId: Int): List<PrintStationProduct> {
        return printStationProductDao.findByPrintStationId(printStationId)
    }
}


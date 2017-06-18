package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStationProduct
import org.springframework.data.repository.CrudRepository

interface PrintStationProductDao : CrudRepository<PrintStationProduct, Int> {
    fun findByPrintStationId(printStationId: Int) : List<PrintStationProduct>
    fun deleteByPrintStationId(id: Int) : Int
}


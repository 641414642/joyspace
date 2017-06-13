package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStation
import org.springframework.data.repository.PagingAndSortingRepository

interface PrintStationDao : PagingAndSortingRepository<PrintStation, Int> {
    fun findByWxQrCode(qrCode: String): PrintStation?
}

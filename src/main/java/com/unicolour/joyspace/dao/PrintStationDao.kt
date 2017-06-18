package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface PrintStationDao : PagingAndSortingRepository<PrintStation, Int> {
    fun findByWxQrCode(qrCode: String): PrintStation?
    fun findBySn(sn: String, pageable: Pageable): Page<PrintStation>
}

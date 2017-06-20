package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface PrintStationDao : PagingAndSortingRepository<PrintStation, Int> {
    fun findByWxQrCode(qrCode: String): PrintStation?

    @Query("SELECT p FROM PrintStation p WHERE LOWER(p.sn) LIKE LOWER(CONCAT('%', :sn, '%'))")
    fun findBySn(@Param("sn") sn: String, pageable: Pageable): Page<PrintStation>
}

package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface PrintStationDao : PagingAndSortingRepository<PrintStation, Int>, PrintStationCustomQuery {
    fun findByWxQrCode(qrCode: String): PrintStation?

    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<PrintStation>
    fun findByCompanyIdAndPositionId(companyId: Int, positionId: Int, pageable: Pageable): Page<PrintStation>
    fun findByCompanyId(companyId: Int): List<PrintStation>

    fun findByAddressCity(cityName: String): List<PrintStation>
    fun findByAddressNation(nationName: String): List<PrintStation>
    fun findByIdIn(idList: List<Int>): List<PrintStation>
    fun findByPositionId(positionId: Int): List<PrintStation>

    @Query("SELECT count(p) > 0 FROM PrintStation p WHERE p.id >= :minId AND p.id <= :maxId")
    fun idExistsInRange(@Param("minId") minId: Int, @Param("maxId") maxId: Int): Boolean
}

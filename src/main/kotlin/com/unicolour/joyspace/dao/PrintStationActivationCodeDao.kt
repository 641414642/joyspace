package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStationActivationCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface PrintStationActivationCodeDao : PagingAndSortingRepository<PrintStationActivationCode, Int> {
    fun existsByCode(code: String): Boolean
    fun findByUsedIsTrue(pageable: Pageable): Page<PrintStationActivationCode>
    fun findByUsedIsFalse(pageable: Pageable): Page<PrintStationActivationCode>
    fun findByCode(code: String): PrintStationActivationCode?

    @Query("SELECT count(p) > 0 FROM PrintStationActivationCode p WHERE p.printStationId >= :minId AND p.printStationId <= :maxId")
    fun printStationIdExistsInRange(@Param("minId") minId: Int, @Param("maxId") maxId: Int): Boolean

    fun findByPrintStationIdBetweenOrderByPrintStationId(startId: Int, endId: Int): List<PrintStationActivationCode>
}

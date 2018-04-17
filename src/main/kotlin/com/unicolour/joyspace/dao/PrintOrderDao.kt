package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface PrintOrderDao : CrudRepository<PrintOrder, Int> {
    fun existsByOrderNo(orderNo: String) : Boolean
    fun findByOrderNo(orderNo: String): PrintOrder?

    @Query("SELECT p FROM PrintOrder p WHERE p.printStationId=:printStationId AND p.payed=true AND p.imageFileUploaded=true AND p.downloadedToPrintStation=false AND p.updateTime>=:updateTime")
    fun findUnDownloadedPrintOrders(
            @Param("printStationId") printStationId: Int,
            @Param("updateTime") updateTime: Calendar): List<PrintOrder>

    fun findByCompanyIdOrderByIdDesc(companyId: Int, pageable: Pageable): Page<PrintOrder>
    fun findByOrderNoIgnoreCaseAndCompanyId(orderNo: String, companyId: Int, pageable: Pageable): Page<PrintOrder>
    fun findByCompanyIdAndPayedIsTrueAndTransferedIsFalse(companyId: Int): List<PrintOrder>
    fun findByUserId(userId: Int): List<PrintOrder>
    fun countByUserIdAndPayedIsFalse(userId: Int): Long
    fun countByUserIdAndPayedIsTrueAndPrintedOnPrintStationIsFalse(userId: Int): Long
    fun countByUserIdAndPrintedOnPrintStationIsTrue(userId: Int): Long
}
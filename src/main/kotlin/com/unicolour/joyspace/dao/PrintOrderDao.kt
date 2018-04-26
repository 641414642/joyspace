package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintOrder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface PrintOrderDao : CrudRepository<PrintOrder, Int>, PrintOrderCustomQuery {
    fun existsByOrderNo(orderNo: String) : Boolean
    fun findByOrderNo(orderNo: String): PrintOrder?

    @Query("SELECT p FROM PrintOrder p WHERE p.printStationId=:printStationId AND p.payed=true AND p.imageFileUploaded=true AND p.downloadedToPrintStation=false AND p.updateTime>=:updateTime")
    fun findUnDownloadedPrintOrders(
            @Param("printStationId") printStationId: Int,
            @Param("updateTime") updateTime: Calendar): List<PrintOrder>

    fun findByCompanyIdAndPayedIsTrueAndTransferedIsFalse(companyId: Int): List<PrintOrder>
}
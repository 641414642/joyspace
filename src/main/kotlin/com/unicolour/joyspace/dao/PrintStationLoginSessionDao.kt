package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStationLoginSession
import org.springframework.data.repository.CrudRepository

interface PrintStationLoginSessionDao : CrudRepository<PrintStationLoginSession, String> {
    fun findByPrintStationId(printStationId: Int) : PrintStationLoginSession?
}
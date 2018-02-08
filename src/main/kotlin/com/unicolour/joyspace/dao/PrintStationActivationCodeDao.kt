package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.PrintStationActivationCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface PrintStationActivationCodeDao : PagingAndSortingRepository<PrintStationActivationCode, Int> {
    fun existsByCode(code: String): Boolean
    fun findByUsedIsTrue(pageable: Pageable): Page<PrintStationActivationCode>
    fun findByUsedIsFalse(pageable: Pageable): Page<PrintStationActivationCode>
    fun findByCode(code: String): PrintStationActivationCode?
}

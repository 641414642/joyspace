package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.AdSet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface AdSetDao : PagingAndSortingRepository<AdSet, Int> {
    fun findByCompanyId(companyId: Int, pageable: Pageable): Page<AdSet>
    fun findByCompanyId(companyId: Int): List<AdSet>
    fun findByNameIgnoreCaseAndCompanyId(name: String, companyId: Int, pageable: Pageable): Page<AdSet>

    /** 返回公用的或指定投放商的广告 */
    @Query("SELECT a FROM AdSet a WHERE a.companyId=0 OR a.companyId=:companyId")
    fun findUsableAdSets(@Param("companyId") companyId: Int): List<AdSet>
}

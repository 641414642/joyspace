package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.CompanyWxAccount
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CompanyWxAccountDao : CrudRepository<CompanyWxAccount, Int> {
    fun findByCompanyIdOrderBySequenceAsc(companyId: Int): List<CompanyWxAccount>
    fun findByVerifyCode(verifyCode: String): CompanyWxAccount?
    fun existsByCompanyIdAndOpenId(companyId: Int, openid: String): Boolean
    fun countByCompanyIdAndEnabledIsTrue(companyId: Int): Int

    @Query("SELECT coalesce(max(a.sequence), 0) FROM CompanyWxAccount a WHERE a.companyId=:companyId AND a.enabled=TRUE")
    fun getMaxAccountSequence(@Param("companyId") companyId: Int): Int
}
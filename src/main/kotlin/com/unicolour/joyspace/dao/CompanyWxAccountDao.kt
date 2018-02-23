package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.CompanyWxAccount
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CompanyWxAccountDao : CrudRepository<CompanyWxAccount, Int> {
    @Query("SELECT a FROM CompanyWxAccount a WHERE a.companyId=:companyId AND a.openId<>'' ORDER BY a.sequence")
    fun getCompanyWxAccounts(@Param("companyId") companyId: Int): List<CompanyWxAccount>

    fun findByVerifyCode(verifyCode: String): CompanyWxAccount?
    fun existsByVerifyCode(verifyCode: String): Boolean

    fun existsByCompanyIdAndOpenId(companyId: Int, openid: String): Boolean

    @Query("SELECT count(a) FROM CompanyWxAccount a WHERE a.companyId=:companyId AND a.openId<>''")
    fun countCompanyWxAccounts(@Param("companyId") companyId: Int): Int

    @Query("SELECT coalesce(max(a.sequence), 0) FROM CompanyWxAccount a WHERE a.companyId=:companyId AND a.openId<>''")
    fun getMaxAccountSequence(@Param("companyId") companyId: Int): Int
}
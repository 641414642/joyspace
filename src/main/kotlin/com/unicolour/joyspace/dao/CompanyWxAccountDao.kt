package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.CompanyWxAccount
import org.springframework.data.repository.CrudRepository

interface CompanyWxAccountDao : CrudRepository<CompanyWxAccount, Int> {
    fun findByCompanyId(companyId: Int): List<CompanyWxAccount>
}
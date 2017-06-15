package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.PriceList

interface CompanyService {
    fun createCompany(name: String, defPriceList: PriceList?) : Company
    fun updateCompany(companyId: Int, name: String): Boolean
}

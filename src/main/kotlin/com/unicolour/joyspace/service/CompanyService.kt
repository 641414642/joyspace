package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.PriceList

interface CompanyService {
    fun createCompany(name: String, defPriceList: PriceList?,
                      username: String,
                      fullname: String,
                      phone: String,
                      email: String,
                      password: String) : Company

    fun updateCompany(companyId: Int, name: String): Boolean
}

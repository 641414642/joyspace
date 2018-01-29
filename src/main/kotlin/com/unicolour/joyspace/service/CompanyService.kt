package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.CompanyWxAccount
import com.unicolour.joyspace.model.PriceList

interface CompanyService {
    fun createCompany(name: String, defPriceList: PriceList?,
                      username: String,
                      fullname: String,
                      phone: String,
                      email: String,
                      password: String,
                      wxPayConfigId: Int) : Company

    fun updateCompany(companyId: Int, name: String, wxPayConfigId: Int): Boolean

    /** 返回可用的微信收款账户 */
    fun getAvailableWxAccount(companyId: Int): CompanyWxAccount?
}

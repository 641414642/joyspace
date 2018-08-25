package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.BusinessModel
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.CompanyWxAccount
import com.unicolour.joyspace.model.PriceList

interface CompanyService {
    fun createCompany(name: String,
                      businessModel: BusinessModel,
                      defPriceList: PriceList?,
                      username: String,
                      fullname: String,
                      phone: String,
                      email: String,
                      password: String) : Company

    fun updateCompany(companyId: Int, name: String, businessModel: BusinessModel, managerId: Int, fullname: String, phone: String, email: String, password: String): Boolean

    /** 返回可用的微信收款账户 */
    fun getAvailableWxAccount(companyId: Int): CompanyWxAccount?

    /** 开始添加微信收款账户
     * @return 返回验证码 */
    fun startAddCompanyWxAccount(): String

    fun addCompanyWxAccount(code: String, realname: String, phoneNumber: String, verifyCode: String)

    fun deleteCompanyWxAccount(accountId: Int): Boolean

    fun moveCompanyWxAccount(id: Int, up: Boolean): Boolean

    fun toggleCompanyWxAccount(id: Int): Boolean

    fun sendVerifyCode(phoneNumber: String): Boolean
}

package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.CompanyWxAccount
import com.unicolour.joyspace.model.PriceList
import javax.transaction.Transactional

interface CompanyService {
    fun createCompany(name: String, defPriceList: PriceList?,
                      username: String,
                      fullname: String,
                      phone: String,
                      email: String,
                      password: String) : Company

    fun updateCompany(companyId: Int, name: String, managerId: Int, fullname: String, phone: String, email: String, password: String): Boolean

    /** 返回可用的微信收款账户 */
    fun getAvailableWxAccount(companyId: Int): CompanyWxAccount?

    /** 开始添加微信收款账户
     * @return 返回验证码 */
    fun startAddCompanyWxAccount(): String

    fun addCompanyWxAccount(code: String, realname: String, phoneNumber: String, verifyCode: String)

    fun deleteCompanyWxAccount(accountId: Int): Boolean
    @Transactional
    fun moveCompanyWxAccount(id: Int, up: Boolean): Boolean
}

package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.controller.GetAccessTokenResult
import com.unicolour.joyspace.controller.GetUserInfoResult
import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.CompanyWxAccountDao
import com.unicolour.joyspace.dao.WxEntTransferRecordDao
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.CompanyWxAccount
import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.ManagerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*
import javax.transaction.Transactional

@Service
open class CompanyServiceImpl : CompanyService {
    companion object {
        val logger = LoggerFactory.getLogger(CompanyServiceImpl::class.java)
    }

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var companyWxAccountDao: CompanyWxAccountDao

    @Autowired
    lateinit var wxEntTransferRecordDao: WxEntTransferRecordDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Transactional
    override fun createCompany(name: String, defPriceList: PriceList?,
                               username: String,
                               fullname: String,
                               phone: String,
                               email: String,
                               password: String): Company {
        if (companyDao.existsByNameIgnoreCase(name)) {
            throw ProcessException(ResultCode.COMPANY_ALREADY_EXISTS, "名称为${name}的投放商已存在")
        }

        val company = Company()

        company.name = name
        company.createTime = Calendar.getInstance()
        company.defaultPriceList = defPriceList
        company.weiXinPayConfig = null

        companyDao.save(company)

        var roles = "ADMIN"
        if (username == "admin") {
            roles = "SUPERADMIN,ADMIN"
        }

        managerService.createManager(username, password, fullname, phone, email, roles, company)

        return company
    }

    override fun updateCompany(companyId: Int, name: String): Boolean {
        val company = companyDao.findOne(companyId)
        if (company != null) {
            company.name = name
            companyDao.save(company)

            return true
        }
        else {
            return false
        }
    }

    private fun getTransferCountOfOpenId(openId: String): Long {
        val startOfToday = Calendar.getInstance()
        startOfToday.set(Calendar.HOUR_OF_DAY, 0)
        startOfToday.set(Calendar.MINUTE, 0)
        startOfToday.set(Calendar.SECOND, 0)
        startOfToday.set(Calendar.MILLISECOND, 0)

        return wxEntTransferRecordDao.countByReceiverOpenIdAndTransferTimeAfter(openId, startOfToday)
    }

    override fun getAvailableWxAccount(companyId: Int): CompanyWxAccount? {
        val accounts = companyWxAccountDao.findByCompanyId(companyId)
        if (!accounts.isEmpty()) {
            for (account in accounts) {
                if (account.enabled) {
                    val transferCount = getTransferCountOfOpenId(account.openId)
                    if (transferCount < 99) {
                        return account
                    }
                }
            }
        }

        return null
    }

    @Transactional
    override fun addCompanyWxAccount(code: String, realname: String, phoneNumber: String, verifyCode: String) {
        val account = companyWxAccountDao.findByVerifyCode(verifyCode)
        if (account == null) {
            throw ProcessException(ResultCode.INVALID_VERIFY_CODE)
        }

        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid={appid}&secret={secret}&code={code}&grant_type=authorization_code",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "appid" to "wxffad6438f08103cb",   //XXX
                        "secret" to "70ba4ab55820f8d1a9766e123e7bcadf",   //XXX
                        "code" to code
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val result = objectMapper.readValue(resp.body, GetAccessTokenResult::class.java)
            if (result.errcode == 0) {
                if (companyWxAccountDao.existsByCompanyIdAndOpenId(account.companyId, result.openid)) {
                    ProcessException(ResultCode.COMPANY_WX_ACCOUNT_EXISTS)
                }

                val userInfo = getUserInfo(result.access_token, result.openid)

                if (userInfo != null) {
                    account.enabled = true
                    account.createTime = Calendar.getInstance()
                    account.avatar = userInfo.headimgurl
                    account.name = realname
                    account.openId = result.openid
                    account.nickName = userInfo.nickname
                    account.phoneNumber = phoneNumber
                    account.verifyCode = ""

                    companyWxAccountDao.save(account)
                    return
                }
            }
            else {
                logger.info("weixin oauth2 call failed, errcode=${result.errcode}, errmsg=${result.errmsg}")
            }
        }

        throw ProcessException(ResultCode.OTHER_ERROR)
    }


    private fun getUserInfo(accessToken: String, openId: String): GetUserInfoResult? {
        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/userinfo?access_token={accessToken}&openid={openId}",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "accessToken" to accessToken,
                        "openId" to openId
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val result = objectMapper.readValue(resp.body, GetUserInfoResult::class.java)
            if (result.errcode == 0) {
                return result
            }
            else {
                logger.info("weixin userinfo call failed, errcode=${result.errcode}, errmsg=${result.errmsg}")
            }
        }

        return null
    }

    override fun startAddCompanyWxAccount() : String {
        val loginManager = managerService.loginManager

        val account = CompanyWxAccount()
        account.companyId = loginManager!!.companyId
        account.openId = ""
        account.name = ""
        account.nickName = ""
        account.phoneNumber = ""
        account.avatar = ""
        account.verifyCode = BigInteger(8 * 8, secureRandom).toString(36).toUpperCase().substring(0, 6)
        account.createTime = Calendar.getInstance()
        account.enabled = false

        companyWxAccountDao.save(account)

        return account.verifyCode
    }
}

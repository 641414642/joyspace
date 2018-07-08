package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.WxGetAccessTokenResult
import com.unicolour.joyspace.dto.WxGetUserInfoResult
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.CompanyWxAccount
import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.model.VerifyCode
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.transaction.Transactional

@Service
open class CompanyServiceImpl : CompanyService {
    companion object {
        val logger = LoggerFactory.getLogger(CompanyServiceImpl::class.java)
    }

    //微信支付关联的公众号appId和appSecret
    @Value("\${com.unicolour.wxmpAppId}")
    lateinit var wxmpAppId: String

    @Value("\${com.unicolour.wxmpAppSecret}")
    lateinit var wxmpAppSecret: String

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var managerDao: ManagerDao

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

    @Autowired
    lateinit var verifyCodeDao: VerifyCodeDao

    @Autowired
    lateinit var smsService: SmsService
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

    @Transactional
    override fun updateCompany(companyId: Int, name: String, managerId: Int, fullname: String, phone: String, email: String, password: String): Boolean {
        val company = companyDao.findOne(companyId)
        if (company != null) {
            company.name = name
            companyDao.save(company)

            val manager = managerService.getCompanyManager(companyId)
            if (manager != null && manager.id == managerId) {
                manager.fullName = fullname
                manager.phone = phone
                manager.email = email
                managerDao.save(manager)

                if (password.isNotBlank()) {
                    managerService.resetPassword(managerId, password)
                }
            }

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
        val accounts = companyWxAccountDao.getCompanyWxAccounts(companyId)
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
        val account = companyWxAccountDao.findByVerifyCode(verifyCode.toUpperCase())
        if (account == null) {
            throw ProcessException(ResultCode.INVALID_VERIFY_CODE)
        }

        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/oauth2/access_token" +
                        "?appid={appid}" +
                        "&secret={secret}" +
                        "&code={code}" +
                        "&grant_type=authorization_code",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "appid" to wxmpAppId,
                        "secret" to wxmpAppSecret,
                        "code" to code
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val result = objectMapper.readValue(resp.body, WxGetAccessTokenResult::class.java)
            if (result.errcode == 0) {
                if (companyWxAccountDao.existsByCompanyIdAndOpenId(account.companyId, result.openid)) {
                    throw ProcessException(ResultCode.COMPANY_WX_ACCOUNT_EXISTS)
                } else if (companyWxAccountDao.countCompanyWxAccounts(account.companyId) >= 10) {
                    throw ProcessException(ResultCode.EXCEED_MAX_WX_ACCOUNT_NUMBER)
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
                    account.sequence = companyWxAccountDao.getMaxAccountSequence(account.companyId) + 1

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


    private fun getUserInfo(accessToken: String, openId: String): WxGetUserInfoResult? {
        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token={accessToken}" +
                        "&openid={openId}",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "accessToken" to accessToken,
                        "openId" to openId
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val result = objectMapper.readValue(resp.body, WxGetUserInfoResult::class.java)
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

    @Transactional
    override fun deleteCompanyWxAccount(accountId: Int): Boolean {
        val account = companyWxAccountDao.findOne(accountId)
        if (account == null) {
            throw ProcessException(ResultCode.INVALID_ACTIVATION_CODE)
        }
        else if (account.companyId != managerService.loginManager!!.companyId) {
            throw ProcessException(ResultCode.OTHER_ERROR, "没有删除权限")
        }

        companyWxAccountDao.delete(accountId)
        return true
    }

    @Transactional
    override fun moveCompanyWxAccount(id: Int, up: Boolean): Boolean {
        val loginManager = managerService.loginManager
        val account = companyWxAccountDao.findOne(id)
        if (account != null && account.companyId == loginManager!!.companyId) {
            val allAccounts = companyWxAccountDao.getCompanyWxAccounts(account.companyId)
            var otherAccount: CompanyWxAccount? = null
            if (up) {
                otherAccount = allAccounts.filter { it.sequence < account.sequence }.sortedByDescending { it.sequence }.firstOrNull()
            }
            else {
                otherAccount = allAccounts.filter { it.sequence > account.sequence }.sortedBy { it.sequence }.firstOrNull()
            }

            if (otherAccount == null) {
                return false
            }
            else {
                val t = account.sequence
                account.sequence = otherAccount.sequence
                otherAccount.sequence = t

                companyWxAccountDao.save(account)
                companyWxAccountDao.save(otherAccount)

                return true
            }
        }
        else {
            return false
        }
    }

    @Transactional
    override fun toggleCompanyWxAccount(id: Int): Boolean {
        val account = companyWxAccountDao.findOne(id)
        val loginManager = managerService.loginManager
        if (account != null && account.companyId == loginManager!!.companyId) {
            account.enabled = !account.enabled
            return true
        }
        else {
            return false
        }
    }

    @Transactional
    override fun sendVerifyCode(phoneNumber: String): Boolean {
        val smsTpl = "【优利绚彩】验证码为:%s,请勿向任何人提供您收到的短信验证码。"
        val now = Instant.now()
        var verifyCode = verifyCodeDao.findOne(phoneNumber)
        if (verifyCode != null) {
            val interval = Duration.between(verifyCode.sendTime.toInstant(), now);
            if (interval.seconds < 60) {
                throw ProcessException(ResultCode.RETRY_LATER)
            } else {
                verifyCode.code = String.format("%06d", secureRandom.nextInt(1000000))
                verifyCode.sendTime = Calendar.getInstance()
                verifyCodeDao.save(verifyCode)

                val sendResult = smsService.send(phoneNumber, String.format(smsTpl, verifyCode.code))
                return if (sendResult.first != 3) {
                    logger.error("Send SMS error, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                    false
                } else {
                    logger.info("Send SMS success, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                    true
                }
            }
        } else {
            verifyCode = VerifyCode()
            verifyCode.phoneNumber = phoneNumber
            verifyCode.code = String.format("%06d", secureRandom.nextInt(1000000))
            verifyCode.sendTime = Calendar.getInstance()
            verifyCodeDao.save(verifyCode)

            val sendResult = smsService.send(phoneNumber, String.format(smsTpl, verifyCode.code))
            return if (sendResult.first != 3) {
                logger.error("Send SMS error, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                false
            } else {
                logger.info("Send SMS success, PhoneNumber: $phoneNumber, ResponseCode: ${sendResult.first}, ResponseId: ${sendResult.second}")
                true
            }
        }
    }
}


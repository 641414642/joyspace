package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.WxMpAccountDao
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.WxGetAccessTokenResult
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.WxMpAccount
import com.unicolour.joyspace.service.WeiXinService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate


@Component
class WeiXinServiceImpl : WeiXinService {
    companion object {
        val logger = LoggerFactory.getLogger(WeiXinServiceImpl::class.java)
    }

    @Autowired
    lateinit var wxMpAccountDao: WxMpAccountDao

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private var appIdToAccessTokenMap: MutableMap<String, Pair<String, Long>> = HashMap()

    override fun sendTextMessage(message: String, openIdList: List<String>, wxMpAccountId: Int, preview: Boolean) {
        val accessToken = getAccessToken(wxMpAccountId)
        if (accessToken == null) {
            throw ProcessException(ResultCode.GET_WX_ACCESS_TOKEN_FAILED)
        }
        else {
            val req = mapOf(
                    "touser" to if (preview) openIdList.first() else openIdList,
                    "msgtype" to "text",
                    "text" to mapOf("content" to message)
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity<String>(objectMapper.writeValueAsString(req), headers)

            val resp = restTemplate.exchange(
                    if (preview) {
                        "https://api.weixin.qq.com/cgi-bin/message/mass/preview?access_token={accessToken}"
                    }
                    else {
                        "https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token={accessToken}"
                    },
                    HttpMethod.POST,
                    entity,
                    String::class.java,
                    mapOf("accessToken" to accessToken)
            )

            if (resp != null && resp.statusCode == HttpStatus.OK) {
                logger.info("Send text message success, message: $message, wxMpAccountId: $wxMpAccountId")
            }
            else {
                throw ProcessException(ResultCode.SEND_WX_TEXT_MESSAGE_FAILED)
            }
        }
    }

    private fun getAccessToken(wxMpAccountId: Int): String? {
        val account = wxMpAccountDao.findOne(wxMpAccountId)
        if (account == null) {
            return null
        }

        synchronized(this) {
            val iter = appIdToAccessTokenMap.entries.iterator()
            val now = System.currentTimeMillis()

            var token: String? = null

            while (iter.hasNext()) {
                val entry = iter.next()
                val expireTime = entry.value.second

                if (now >= expireTime) {
                    iter.remove()
                }

                if (entry.key == account.appId) {
                    token = entry.value.first
                }
            }

            return token ?: getAccessTokenImpl(account)
        }
    }
/*
    override fun createWxQrCode(scene: String, page: String, width: Int): String {
        val token = managerAppAccessToken
        if (token != null) {
            val url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=$token"

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON_UTF8

            val requestJson =
"""{
"scene":"$scene",
"width":$width,
"auto_color":true
}"""
//"page":"$page",
            val entity = HttpEntity<String>(requestJson, headers)

            val resp = restTemplate.postForEntity(url, entity, ByteArray::class.java)
            if (resp != null && resp.statusCode == HttpStatus.OK) {
                return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(resp.body)
            }
        }

        return ""
    }
    */

    private fun getAccessTokenImpl(account: WxMpAccount): String? {
        val appId = account.appId
        val appSecret = account.wxAppSecret

        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type={grant_type}&appid={appid}&secret={secret}",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "appid" to appId,
                        "secret" to appSecret,
                        "grant_type" to "client_credential"
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val bodyStr = resp.body
            val body: WxGetAccessTokenResult = objectMapper.readValue(bodyStr, WxGetAccessTokenResult::class.java)

            if (body.errcode == 0 && body.access_token.isNotEmpty()) {
                val token = body.access_token
                val expireTime = body.expires_in * 1000 - 60000
                appIdToAccessTokenMap[account.appId] = token to expireTime.toLong()

                return token
            } else {
                logger.warn("Get wx access token for appId: $appId failed, server response body: $body")
                appIdToAccessTokenMap.remove(account.appId)
            }
        }
        else {
            logger.warn("Get wx access token for appId: $appId failed, server response status code: ${resp?.statusCode}, response body: ${resp?.body}")
        }

        return null
    }
}


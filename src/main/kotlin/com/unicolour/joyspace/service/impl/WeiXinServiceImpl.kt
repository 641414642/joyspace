package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.service.WeiXinService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*


@Component
class WeiXinServiceImpl : WeiXinService {
    @Autowired
    lateinit var restTemplate: RestTemplate

    @Value("\${com.unicolour.wxAppId}")
    lateinit var wxAppId: String

    @Value("\${com.unicolour.wxAppSecret}")
    lateinit var wxAppSecret: String

    @Value("\${com.unicolour.wxManagerAppId}")
    lateinit var wxManagerAppId: String

    @Value("\${com.unicolour.wxManagerAppSecret}")
    lateinit var wxManagerAppSecret: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private var _appAccessToken: String? = null
    private var _appAccessTokenExpire: Long = 0

    private var _managerAppAccessToken: String? = null
    private var _managerAppAccessTokenExpire: Long = 0

    val appAccessToken: String? get() {
        synchronized(this, {
            if (_appAccessToken == null || System.currentTimeMillis() >= _appAccessTokenExpire) {
                getAccsssToken(false)
            }
            return _appAccessToken
        })
    }

    val managerAppAccessToken: String? get() {
        synchronized(this, {
            if (_managerAppAccessToken == null || System.currentTimeMillis() >= _managerAppAccessTokenExpire) {
                getAccsssToken(true)
            }
            return _managerAppAccessToken
        })
    }

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

    private fun getAccsssToken(managerApp: Boolean) {
        val appId = if (managerApp) wxManagerAppId else wxAppId
        val appSecret = if (managerApp) wxManagerAppSecret else wxAppSecret

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
            val body: GetAccessTokenResult = objectMapper.readValue(bodyStr, GetAccessTokenResult::class.java)

            if (body.errcode == 0 && !body.access_token.isNullOrEmpty()) {
                if (managerApp) {
                    _managerAppAccessToken = body.access_token
                    _managerAppAccessTokenExpire = System.currentTimeMillis() + body.expires_in!! * 1000 - 60000
                }
                else {
                    _appAccessToken = body.access_token
                    _appAccessTokenExpire = System.currentTimeMillis() + body.expires_in!! * 1000 - 60000
                }
            } else {
                if (managerApp) {
                    _managerAppAccessToken = null
                    _managerAppAccessTokenExpire = 0
                }
                else {
                    _appAccessToken = null
                    _appAccessTokenExpire = 0
                }
            }
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class GetAccessTokenResult(
        var access_token: String? = null,
        var expires_in: Int? = 0,
        var errcode: Int? = 0,
        var errmsg: String? = null
)

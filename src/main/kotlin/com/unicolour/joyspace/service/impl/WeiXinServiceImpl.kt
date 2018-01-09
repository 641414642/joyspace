package com.unicolour.joyspace.service.impl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.service.WeiXinService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import java.nio.file.Paths


@Component
class WeiXinServiceImpl : WeiXinService {
    @Autowired
    lateinit var restTemplate: RestTemplate

    @Value("\${com.unicolour.wxAppId}")
    lateinit var wxAppId: String

    @Value("\${com.unicolour.wxAppSecret}")
    lateinit var wxAppSecret: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private var _accessToken: String? = null
    private var _accessTokenExpire: Long = 0

    override val accessToken: String? get() {
        synchronized(this, {
            if (_accessToken == null || System.currentTimeMillis() >= _accessTokenExpire) {
                getAccsssToken()
            }
            return _accessToken
        })
    }

    override fun createWxQrCode() {
        val token = accessToken
        if (token != null) {
            val url = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=$token"

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON_UTF8

            val requestJson =
"""{
"scene":"abcdefg",
"page":"pages/index/index",
"width":430,
"auto_color":true
}"""
            val entity = HttpEntity<String>(requestJson, headers)

            val resp = restTemplate.postForEntity(url, entity, ByteArray::class.java)
            if (resp != null && resp.statusCode == HttpStatus.OK) {
                Files.write(Paths.get("R:\\test.png"), resp.body)
            }
        }
    }

    private fun getAccsssToken() {
        val resp = restTemplate.exchange(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type={grant_type}&appid={appid}&secret={secret}",
                HttpMethod.GET,
                null,
                String::class.java,
                mapOf(
                        "appid" to wxAppId,
                        "secret" to wxAppSecret,
                        "grant_type" to "client_credential"
                )
        )

        if (resp != null && resp.statusCode == HttpStatus.OK) {
            val bodyStr = resp.body
            val body: GetAccessTokenResult = objectMapper.readValue(bodyStr, GetAccessTokenResult::class.java)

            if (body.errcode == 0 && !body.access_token.isNullOrEmpty()) {
                _accessToken = body.access_token
                _accessTokenExpire = System.currentTimeMillis() + body.expires_in!! * 1000 - 60000
            } else {
                _accessToken = null
                _accessTokenExpire = 0
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

package com.unicolour.joyspace.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContextBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.StandardPasswordEncoder
import org.springframework.web.client.RestTemplate
import java.security.KeyStore
import java.security.SecureRandom


@Configuration
@EnableScheduling
open class BeanConfiguration {
    @Value("\${com.unicolour.wxMchId}")  //商户id
    lateinit var wxMchId: String

    @Bean
    open fun passwordEncoder(): PasswordEncoder = StandardPasswordEncoder("R#0P=V0ut@Bx.Pd7")

    @Bean
    open fun taskScheduler(): TaskScheduler = ThreadPoolTaskScheduler()

    @Bean
    open fun objectMapper() = ObjectMapper()

    @Bean
    open fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        if (wxMchId.isBlank()) {
            return builder
                    .requestFactory(HttpComponentsClientHttpRequestFactory())
                    .build()
        }
        else {
            val password = wxMchId.toCharArray()
            val keyStore = KeyStore.getInstance("PKCS12")

            BeanConfiguration::class.java.getResourceAsStream("/apiclient_cert.p12").use {
                keyStore.load(it, password)
            }

            val sslContext = SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore, password)
                    .loadTrustMaterial(null, TrustSelfSignedStrategy()).build()

            val client = HttpClients.custom().setSSLContext(sslContext).build()
            return builder
                    .requestFactory(HttpComponentsClientHttpRequestFactory(client))
                    .build()
        }
    }

    @Bean
    open fun secureRandom() = SecureRandom()
}

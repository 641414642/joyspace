package com.unicolour.joyspace.config

import com.fasterxml.jackson.databind.ObjectMapper
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
import java.security.SecureRandom


@Configuration
@EnableScheduling
open class BeanConfiguration {
    @Bean
    open fun passwordEncoder(): PasswordEncoder = StandardPasswordEncoder("R#0P=V0ut@Bx.Pd7")

    @Bean
    open fun taskScheduler(): TaskScheduler = ThreadPoolTaskScheduler()

    @Bean
    open fun objectMapper() = ObjectMapper()

    @Bean
    open fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.requestFactory(HttpComponentsClientHttpRequestFactory()).build()
    }

    @Bean
    open fun secureRandom() = SecureRandom()
}

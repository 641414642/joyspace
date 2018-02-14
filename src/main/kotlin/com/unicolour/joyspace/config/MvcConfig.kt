package com.unicolour.joyspace.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
open class MvcConfig : WebMvcConfigurerAdapter() {
    @Autowired
    lateinit var sysHandlerInterceptor: SystemHandlerInterceptor

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/home").setViewName("layout")
        registry.addViewController("/").setViewName("layout")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sysHandlerInterceptor)
    }
}
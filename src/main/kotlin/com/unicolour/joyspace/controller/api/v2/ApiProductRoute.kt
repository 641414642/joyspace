package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dto.HomePageVo
import com.unicolour.joyspace.dto.ProductVo
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.service.AdSetService
import com.unicolour.joyspace.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiProductRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var adSetService: AdSetService
    @Autowired
    private lateinit var productService: ProductService

    /**
     * 主页数据
     */
    @GetMapping(value = "/v2/homepage")
    fun showHomePage(): RestResponse {
        val homePage = HomePageVo()
        return RestResponse.ok(homePage)
    }


    /**
     * 获取某个类型的全部产品（规格／模版）信息
     */
    @GetMapping(value = "/v2/product/{type}")
    fun getProductsByType(@PathVariable("type") type: Int): RestResponse {
        val productVo = ProductVo()
        return RestResponse.ok(productVo)
    }

}
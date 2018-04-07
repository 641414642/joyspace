package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dto.common.RestResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiPrintStationRoute {
    val logger = LoggerFactory.getLogger(this::class.java)


    /**
     * 主页数据
     */
    @GetMapping(value = "/v2/printStation/findByQrCode")
    fun getPrintStationByQrCode(): RestResponse {

        return RestResponse.ok()
    }

}
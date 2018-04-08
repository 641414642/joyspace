package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dto.PrintStationVo
import com.unicolour.joyspace.dto.common.RestResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiPrintStationRoute {
    val logger = LoggerFactory.getLogger(this::class.java)


    /**
     * 根据二维码查找自助机
     */
    @GetMapping(value = "/v2/printStation/findByQrCode")
    fun getPrintStationByQrCode(): RestResponse {
        val printStation = PrintStationVo()
        return RestResponse.ok(printStation)
    }


    /**
     * 获取最近的自助机
     */
    @GetMapping(value = "/v2/printStation/nearest")
    fun getNearest(): RestResponse {
        val printStations = mutableListOf<PrintStationVo>()
        return RestResponse.ok(printStations)
    }

}
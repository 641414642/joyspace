package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dto.PrintStationProduct
import com.unicolour.joyspace.dto.PrintStationVo
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.common.RestResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiPrintStationRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var printStationProductDao: PrintStationProductDao
    @Autowired
    private lateinit var printStationDao: PrintStationDao


    /**
     * 根据二维码查找自助机
     */
    @GetMapping(value = "/v2/printStation/findByQrCode")
    fun getPrintStationByQrCode(@RequestParam(required = false, value = "qrCode") qrcode: String?): RestResponse {
        val printStation = printStationDao.findByWxQrCode("https://joyspace1.uni-colour.com/printStation/9909")
                ?: return RestResponse.error(ResultCode.PRINT_STATION_NOT_FOUND)
        val psVo = PrintStationVo()
        psVo.id = printStation.id
        psVo.address = printStation.addressNation + printStation.addressProvince + printStation.addressCity + printStation.addressDistrict + printStation.addressStreet
        psVo.longitude = printStation.position.longitude
        psVo.latitude = printStation.position.latitude
        psVo.wxQrCode = printStation.wxQrCode
        psVo.positionId = printStation.positionId.toString()
        psVo.companyId = printStation.companyId.toString()
        psVo.status = printStation.status

        psVo.products = printStationProductDao.findByPrintStationId(printStation.id).map {
            PrintStationProduct(it.productId, it.product.name, it.product.template.type.toString(), it.product.defaultPrice)
        }.toMutableList()
        return RestResponse.ok(psVo)
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
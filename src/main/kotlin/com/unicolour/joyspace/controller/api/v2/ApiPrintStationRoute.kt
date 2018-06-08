package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.TPriceDao
import com.unicolour.joyspace.dto.PrintStationProduct
import com.unicolour.joyspace.dto.PrintStationVo
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.TPriceItemVo
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.service.PositionService
import com.unicolour.joyspace.service.PrintStationService
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
    @Autowired
    private lateinit var printStationService: PrintStationService
    @Autowired
    private lateinit var positionService:PositionService
    @Autowired
    private lateinit var productDao:ProductDao
    @Autowired
    private lateinit var tPriceDao: TPriceDao


    /**
     * 根据二维码查找自助机
     */
    @GetMapping(value = "/v2/printStation/findByQrCode")
    fun getPrintStationByQrCode(@RequestParam("qrCode") qrcode: String): RestResponse {
        val printStation = printStationDao.findByWxQrCode(qrcode)
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
        psVo.name = printStation.name
        psVo.imgUrl = ""

        val priceMap: Map<Int, Int> = printStationService.getPriceMap(printStation)
        psVo.products = printStationProductDao.findByPrintStationId(printStation.id).map {
            val price = priceMap.getOrDefault(it.productId, it.product.defaultPrice)
            val tPrice = tPriceDao.findByCompanyIdAndProductId(printStation.companyId,it.productId)
            val tPriceItemVoList = mutableListOf<TPriceItemVo>()
            tPrice?.tPriceItems?.forEach {
                tPriceItemVoList.add(TPriceItemVo(it.minCount,it.maxCount,it.price))
            }
            PrintStationProduct(it.productId, it.product.name, it.product.template.type.toString(), price,tPriceItemVoList)
        }.toMutableList()
        val tProduct = productDao.findOne(9528)
        val tPrice = priceMap.getOrDefault(tProduct.id,tProduct.defaultPrice)
        psVo.products!!.add(PrintStationProduct(9528,tProduct.name,"2",tPrice))
        return RestResponse.ok(psVo)
    }


    /**
     * 获取最近的自助机
     */
    @GetMapping(value = "/v2/printStation/nearest")
    fun getNearest(@RequestParam("longitude") longitude: Double,
                   @RequestParam("latitude") latitude: Double): RestResponse {


        val addressComponent = positionService.getAddressComponent(longitude, latitude)
        if (addressComponent == null) {
            return RestResponse.error(ResultCode.PRINT_STATION_NOT_FOUND)
        } else {
            val printStations = printStationDao.findByAddressCity(addressComponent.city)
            val nearest = printStations.minBy { distance(longitude, latitude, it.position.longitude, it.position.latitude) }

            if (nearest == null) {
                return RestResponse.error(ResultCode.PRINT_STATION_NOT_FOUND)
            } else {
                val psVo = PrintStationVo()
                psVo.id = nearest.id
                psVo.address = nearest.addressNation + nearest.addressProvince + nearest.addressCity + nearest.addressDistrict + nearest.addressStreet
                psVo.longitude = nearest.position.longitude
                psVo.latitude = nearest.position.latitude
                psVo.wxQrCode = nearest.wxQrCode
                psVo.positionId = nearest.positionId.toString()
                psVo.companyId = nearest.companyId.toString()
                psVo.status = nearest.status
                psVo.name = nearest.name
                psVo.imgUrl = ""

                val priceMap: Map<Int, Int> = printStationService.getPriceMap(nearest)
                psVo.products = printStationProductDao.findByPrintStationId(nearest.id).map {
                    val price = priceMap.getOrDefault(it.productId, it.product.defaultPrice)
                    val tPrice = tPriceDao.findByCompanyIdAndProductId(nearest.companyId,it.productId)
                    val tPriceItemVoList = mutableListOf<TPriceItemVo>()
                    tPrice?.tPriceItems?.forEach {
                        tPriceItemVoList.add(TPriceItemVo(it.minCount,it.maxCount,it.price))
                    }
                    PrintStationProduct(it.productId, it.product.name, it.product.template.type.toString(), price,tPriceItemVoList)
                }.toMutableList()
                val tProduct = productDao.findOne(9528)
                val tPrice = priceMap.getOrDefault(tProduct.id,tProduct.defaultPrice)
                psVo.products!!.add(PrintStationProduct(9528,tProduct.name,"2",tPrice))
                return RestResponse.ok(psVo)
            }
        }
    }


    /**
     * 获取附近的自助机
     */
    @GetMapping(value = "/v2/printStation/nearby")
    fun getNearByList(@RequestParam("longitude", required = false) longitude: Double?,
                      @RequestParam("latitude", required = false) latitude: Double?): RestResponse {
//        var city = ""
//        if (longitude != null && latitude != null) {
//            val addressComponent = positionService.getAddressComponent(longitude, latitude)
//            addressComponent?.let {
//                city = addressComponent.city
//            }
//        }

        val printStations = printStationDao.findByAddressNation("中国")
        val resultList = printStations.map { printStation ->
            val psVo = PrintStationVo()
            psVo.id = printStation.id
            psVo.address = printStation.addressNation + printStation.addressProvince + printStation.addressCity + printStation.addressDistrict + printStation.addressStreet
            psVo.longitude = printStation.position.longitude
            psVo.latitude = printStation.position.latitude
            psVo.wxQrCode = printStation.wxQrCode
            psVo.positionId = printStation.positionId.toString()
            psVo.companyId = printStation.companyId.toString()
            psVo.status = printStation.status
            psVo.name = printStation.name
            psVo.imgUrl = ""
            psVo
        }


        return RestResponse.ok(resultList)
    }

    private val AVERAGE_RADIUS_OF_EARTH_M = 6371000

    fun distance(long1: Double, lat1: Double,
                 long2: Double, lat2: Double): Double {

        val latDistance = Math.toRadians(lat1 - lat2)
        val lngDistance = Math.toRadians(long1 - long2)

        val sinLatDist = Math.sin(latDistance / 2)
        val sinLongDist = Math.sin(lngDistance / 2)

        val a = sinLatDist * sinLatDist +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        sinLongDist * sinLongDist

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return AVERAGE_RADIUS_OF_EARTH_M * c
    }

}
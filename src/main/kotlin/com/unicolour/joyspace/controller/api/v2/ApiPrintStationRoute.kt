package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.TPriceDao
import com.unicolour.joyspace.dto.PrintStationProduct
import com.unicolour.joyspace.dto.PrintStationVo
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.TPriceItemVo
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.service.CouponService
import com.unicolour.joyspace.service.PositionService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

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
    private lateinit var tPriceDao: TPriceDao
    @Autowired
    private lateinit var couponService: CouponService
    @Autowired
    private lateinit var productService: ProductService


    /**
     * 根据二维码查找自助机
     */
    @GetMapping(value = "/v2/printStation/findByQrCode")
    fun getPrintStationByQrCode(@RequestParam("qrCode") qrcode: String,
                                @RequestParam("sessionId", required = false) sessionId: String?): RestResponse {
        val printStation = printStationDao.findByWxQrCode(qrcode)
                ?: return RestResponse.error(ResultCode.PRINT_STATION_NOT_FOUND)
        val psVo = printStationService.toPrintStationVo(printStation)
        val priceMap: Map<Int, Int> = printStationService.getPriceMap(printStation)
        psVo.products = productService.getProductsOfPrintStationAndCommonProduct(printStation.id).map {
            val price = priceMap.getOrDefault(it.id, it.defaultPrice)
            val tPrice = tPriceDao.findByPositionIdAndProductIdAndBeginLessThanAndExpireGreaterThanAndEnabled(printStation.positionId, it.id, Date(), Date(), true).firstOrNull()
            val tPriceItemVoList = mutableListOf<TPriceItemVo>()
            tPrice?.tPriceItems?.forEach {
                tPriceItemVoList.add(TPriceItemVo(it.minCount,it.maxCount,it.price))
            }
            val couponSign = couponService.beCouponProduct(sessionId ?: "", it.id)
            PrintStationProduct(it.id, it.name, it.template.type.toString(), price, tPriceItemVoList, if (couponSign) 1 else 0)
        }.toMutableList()
//        val tProduct = productDao.findOne(9528)
//        val tPrice = priceMap.getOrDefault(tProduct.id,tProduct.defaultPrice)
//        psVo.products!!.add(PrintStationProduct(9528,tProduct.name,"2",tPrice))
        return RestResponse.ok(psVo)
    }


    /**
     * 获取最近的自助机
     */
    @GetMapping(value = "/v2/printStation/nearest")
    fun getNearest(@RequestParam("longitude") longitude: Double,
                   @RequestParam("latitude") latitude: Double,
                   @RequestParam("sessionId", required = false) sessionId: String?): RestResponse {


        val addressComponent = positionService.getAddressComponent(longitude, latitude)
        if (addressComponent == null) {
            return RestResponse.error(ResultCode.PRINT_STATION_NOT_FOUND)
        } else {
            val printStations = printStationDao.findByAddressCity(addressComponent.city)
            val nearest = printStations.minBy { distance(longitude, latitude, it.position.longitude, it.position.latitude) }

            if (nearest == null) {
                return RestResponse.error(ResultCode.PRINT_STATION_NOT_FOUND)
            } else {
                val psVo = printStationService.toPrintStationVo(nearest)
                val priceMap: Map<Int, Int> = printStationService.getPriceMap(nearest)
                psVo.products = productService.getProductsOfPrintStationAndCommonProduct(nearest.id).map {
                    val price = priceMap.getOrDefault(it.id, it.defaultPrice)
                    val tPrice = tPriceDao.findByPositionIdAndProductIdAndBeginLessThanAndExpireGreaterThanAndEnabled(nearest.positionId, it.id, Date(), Date(), true).firstOrNull()
                    val tPriceItemVoList = mutableListOf<TPriceItemVo>()
                    tPrice?.tPriceItems?.forEach {
                        tPriceItemVoList.add(TPriceItemVo(it.minCount,it.maxCount,it.price))
                    }
                    val couponSign = couponService.beCouponProduct(sessionId ?: "", it.id)
                    PrintStationProduct(it.id, it.name, it.template.type.toString(), price, tPriceItemVoList, if (couponSign) 1 else 0)
                }.toMutableList()
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
            printStationService.toPrintStationVo(printStation)
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
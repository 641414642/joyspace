package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
class ApiPrintStationController {
    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationService: PrintStationService

    @Autowired
    lateinit var productService: ProductService

    @RequestMapping("/api/printStation/findByQrCode", method = arrayOf(RequestMethod.GET))
    fun findByQrCode(request: HttpServletRequest, @RequestParam("qrCode") qrCode: String) : ResponseEntity<PrintStationDTO> {
        val printStation: PrintStation? = printStationDao.findByWxQrCode(qrCode);

        if (printStation == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            val baseUrl = getBaseUrl(request)
            val priceMap: Map<Int, Int> = printStationService.getPriceMap(printStation)
            val productsOfPrintStation: List<ProductDTO> =
                    productService.getProductsOfPrintStation(printStation.id).map { it.product.productToDTO(baseUrl, priceMap) }

            val ps: PrintStationDetailDTO = printStation.printStationToDetailDTO(productsOfPrintStation)
            return ResponseEntity.ok(ps)
        }
    }

    @RequestMapping("/api/printStation/{id}", method = arrayOf(RequestMethod.GET))
    fun findById(request: HttpServletRequest, @PathVariable("id") id: Int) : ResponseEntity<PrintStationDTO> {
        val printStation: PrintStation? = printStationDao.findOne(id);

        if (printStation == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            val baseUrl = getBaseUrl(request)
            val priceMap: Map<Int, Int> = printStationService.getPriceMap(printStation)
            val productsOfPrintStation: List<ProductDTO> =
                    productService.getProductsOfPrintStation(printStation.id).map { it.product.productToDTO(baseUrl, priceMap) }

            val ps: PrintStationDetailDTO = printStation.printStationToDetailDTO(productsOfPrintStation)
            return ResponseEntity.ok(ps)
        }
    }
}


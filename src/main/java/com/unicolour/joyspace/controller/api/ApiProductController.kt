package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dto.ProductDTO
import com.unicolour.joyspace.dto.productToDTO
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
class ApiProductController {
    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var printStationService: PrintStationService

    @RequestMapping("/api/product/findByPrintStation", method = arrayOf(RequestMethod.GET))
    fun findByPrintStation(
            request: HttpServletRequest,
            @RequestParam("printStationId") prnStationId: Int) : ResponseEntity<List<ProductDTO>> {

        val baseUrl = getBaseUrl(request)
        val printStation = printStationDao.findOne(prnStationId)
        if (printStation == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            val priceMap: Map<Int, Int> = printStationService.getPriceMap(printStation);

            val products = productDao.findAll()
            return ResponseEntity.ok(products.map { it.productToDTO(baseUrl, priceMap) }.toList())
        }
    }
}

package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dto.ProductDTO
import com.unicolour.joyspace.dto.TemplateImageInfo
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.dto.productToDTO
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.TemplateService
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
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

    @Autowired
    lateinit var templateService: TemplateService

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

    @RequestMapping("/api/product/images", method = arrayOf(RequestMethod.GET))
    fun productImages(@RequestParam("productId") productId: Int): ResponseEntity<List<TemplateImageInfo>> {
        val product = productDao.findOne(productId)
        if (product == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            val tplInfo = templateService.getTemplateInfo(product.templateName)
            if (tplInfo == null) {
                return ResponseEntity.notFound().build()
            }
            else {
                return ResponseEntity.ok(tplInfo.images)
            }
        }
    }

    @RequestMapping("/api/product/preview", method = arrayOf(RequestMethod.POST))
    fun productPreview(
            request: HttpServletRequest,
            @RequestBody previewParam: PreviewParam): ResponseEntity<TemplatePreviewResult> {

        val product = productDao.findOne(previewParam.productId)
        if (product == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            val tplInfo = templateService.getTemplateInfo(product.templateName)
            if (tplInfo == null) {
                return ResponseEntity.notFound().build()
            }
            else {
                val baseUrl = getBaseUrl(request)

                return ResponseEntity.ok(
                        templateService.createPreview(previewParam, product.templateName, baseUrl))
            }
        }
    }
}

class ImageParam(
        var name: String = "",
        var imageId: Int = 0
)

class PreviewParam(
        var sessionId: String = "",
        var productId: Int = 0,
        var images: List<ImageParam> = emptyList()
)

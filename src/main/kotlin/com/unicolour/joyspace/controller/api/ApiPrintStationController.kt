package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.PrintStation
import com.unicolour.joyspace.service.GraphQLService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.util.getBaseUrl
import graphql.GraphQL
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

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/api/printStation/findByDistance", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun findByDistance(
            request: HttpServletRequest,
            @RequestParam("longitude") longitude: Double,
            @RequestParam("latitude") latitude: Double,
            @RequestParam("radius") radius:Int) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
                """
query {
	findPrintStationsByDistance(
            longitude:$longitude,
            latitude:$latitude,
            radius:$radius) {
        printStations {
                id
                address
                longitude
                latitude
                wxQrCode
        }
	}
}
"""
        val context = hashMapOf<String, Any>( "baseUrl" to getBaseUrl(request))
        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        val result = data["findPrintStationsByDistance"] as? Map<*, *>
        return result?.get("printStations")
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


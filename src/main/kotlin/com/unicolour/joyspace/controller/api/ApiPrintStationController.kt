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

    @RequestMapping("/api/printStation/findByQrCode", method = arrayOf(RequestMethod.GET))
    fun findByQrCode(request: HttpServletRequest, @RequestParam("qrCode") qrCode: String) : Any? {
        val printStation: PrintStation? = printStationDao.findByWxQrCode(qrCode);

        return findById(request, printStation?.id ?: 0);
    }

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/api/printStation/{id}", method = arrayOf(RequestMethod.GET))
    fun findById(request: HttpServletRequest, @PathVariable("id") id: Int) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
                """
query {
	printStation(printStationId:$id) {
        id
        address
        longitude
        latitude
        wxQrCode
		products {
			id
			name
			type:typeInt
            version
            templateWidth
            templateHeight
			width
			height
            displaySize
			imageRequired
			remark
			price
			thumbnailImageUrl
			previewImageUrls
            templateImages {
                name
                x:tx
                y:ty
                width:tw
                height:th
                isUserImage:userImage
                url
            }
		}
	}
}
"""
        val context = hashMapOf<String, Any>("baseUrl" to getBaseUrl(request))

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["printStation"]
    }
}


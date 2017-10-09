package com.unicolour.joyspace.controller.app

import com.unicolour.joyspace.service.GraphQLService
import com.unicolour.joyspace.util.getBaseUrl
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
class AppPrintStationController {

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/app/printStation/findByCity", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun findByCity(
            request: HttpServletRequest,
            @RequestParam("longitude") longitude: Double,
            @RequestParam("latitude") latitude: Double) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
query {
	findPrintStationsByCity(
            longitude:$longitude,
            latitude:$latitude) {
        state: result
        msg: description
        result: printStations {
                id
                name
                address
                longitude
                latitude
                transportation
                distance
        }
	}
}
"""
        val context = hashMapOf<String, Any>(
                "baseUrl" to getBaseUrl(request),
                "refLatitude" to latitude,
                "refLongitude" to longitude)

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["findPrintStationsByCity"]
    }

    @RequestMapping("/app/printStation/nearest", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun findNearest(
            request: HttpServletRequest,
            @RequestParam("longitude") longitude: Double,
            @RequestParam("latitude") latitude: Double) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
query {
	findNearestPrintStation(
            longitude:$longitude,
            latitude:$latitude) {
        state: result
        msg: description
        result: printStation {
                id
                name
                address
                longitude
                latitude
                transportation
                distance
        }
	}
}
"""
        val context = hashMapOf<String, Any>(
                "baseUrl" to getBaseUrl(request),
                "refLatitude" to latitude,
                "refLongitude" to longitude)

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["findNearestPrintStation"]
    }

    @RequestMapping("/app/printStation/{id}", method = arrayOf(RequestMethod.GET))
    fun findById(request: HttpServletRequest, @PathVariable("id") id: Int) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
query {
	printStation(printStationId:${id}) {
        id
		address
		wxQrCode
		longitude
		latitude
        transportation
		products {
			id
			name
			type
			width
			height
			imageCount: imageRequired
			remark
			price
			thumbnailUrl
			previewUrls
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
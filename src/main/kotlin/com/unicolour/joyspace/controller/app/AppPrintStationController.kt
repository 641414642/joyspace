package com.unicolour.joyspace.controller.app

import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
class AppPrintStationController {

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/app/printStation/ofSameCity", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun ofSameCity(
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
                positionId
                companyId
                address
                longitude
                latitude
                transportation
                distance
                images
        }
	}
}
"""
        val context = hashMapOf<String, Any>(
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
                positionId
                companyId
                address
                longitude
                latitude
                transportation
                distance
                images
        }
	}
}
"""
        val context = hashMapOf<String, Any>(
                "refLatitude" to latitude,
                "refLongitude" to longitude)

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["findNearestPrintStation"]
    }

    @RequestMapping("/app/printStation/{id}/products", method = arrayOf(RequestMethod.GET))
    fun printStationProducts(request: HttpServletRequest, @PathVariable("id") id: Int) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
"""
query {
	printStation(printStationId:$id) {
		products {
			id
			name
			type
			width
			height
			imageCount: imageRequired
			remark
			price
			thumbnailImageUrl
			previewImageUrls
            templateUrl
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
        val context = HashMap<String, Any>()

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        val printStation = data["printStation"] as? Map<*, *>?
        return printStation?.get("products")
    }
}
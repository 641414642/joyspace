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

    @RequestMapping("/app/printStation/findByDistance", method = arrayOf(RequestMethod.GET))
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
        state: result
        msg: description
        result: printStations {
                id
                address
                longitude
                latitude
        }
	}
}
"""
        val context = hashMapOf<String, Any>( "baseUrl" to getBaseUrl(request))
        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["findPrintStationsByDistance"]
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
        val context = hashMapOf<String, Any>( "baseUrl" to getBaseUrl(request))
        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["printStation"]
    }

}
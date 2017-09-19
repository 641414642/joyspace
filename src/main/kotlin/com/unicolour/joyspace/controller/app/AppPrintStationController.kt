package com.unicolour.joyspace.controller.app

import com.unicolour.joyspace.service.GraphQLService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class AppPrintStationController {

    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/app/printStation/findByDistance", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun findByDistance(
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
        val queryResult = graphQL.execute(query, null, null, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["findPrintStationsByDistance"]
    }
}
package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.service.GraphQLService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiCouponController {
    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/api/user/coupons", method = arrayOf(RequestMethod.GET))
    fun getUserCoupons(@RequestParam("sessionId") sessionId: String) : Any? {
        val schema = graphQLService.getGraphQLSchema()
        val graphQL = GraphQL.newGraphQL(schema).build()

        val query =
                """
query {
	userCouponList(sessionId:"$sessionId") {
        errcode:result
        errmsg:description
        coupons {
            id
            name
            code
            begin
            expire
            minExpense
            discount
		}
	}
}
"""
        val context = HashMap<String, Any>()

        val queryResult = graphQL.execute(query, null, context, emptyMap())
        val data:Map<String, Any> = queryResult.getData()
        return data["userCouponList"]
    }
}
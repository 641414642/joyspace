package com.unicolour.joyspace.controller.graphql

import com.unicolour.joyspace.service.GraphQLService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RestController
class GraphQLController {
    @Autowired
    lateinit var graphQLService: GraphQLService

    @RequestMapping("/graphql", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun graphQLQuery(request: HttpServletRequest) : Any {
        val schema = graphQLService.getGraphQLSchema()

        val graphQL = GraphQL.newGraphQL(schema)
//                .queryExecutionStrategy(ExecutorServiceExecutionStrategy(threadPoolExecutor))
//                .mutationExecutionStrategy(SimpleExecutionStrategy())
                .build()

        val query = request.getParameter("query")
        val result = graphQL.execute(query)

        return result
    }

    @RequestMapping("/graphql",
            method = arrayOf(RequestMethod.POST),
            consumes = arrayOf(APPLICATION_JSON_VALUE, "application/graphql"))
    @ResponseBody
    fun graphQLMutation(request: HttpServletRequest, graphQLRequest: GraphQLRequest?) : Any {
        val schema = graphQLService.getGraphQLSchema()

        val graphQL = GraphQL.newGraphQL(schema)
//                .queryExecutionStrategy(ExecutorServiceExecutionStrategy(threadPoolExecutor))
//                .mutationExecutionStrategy(SimpleExecutionStrategy())
                .build()

        var query = request.getParameter("query")
        if (query.isNullOrBlank()) {
            query = if (graphQLRequest == null) "" else graphQLRequest.query
        }

        val result = graphQL.execute(query)

        return result
    }
}

data class GraphQLRequest(
    var query: String = "",
    var operationName: String? = "",
    var variables: Map<String, Any>? = emptyMap()
)

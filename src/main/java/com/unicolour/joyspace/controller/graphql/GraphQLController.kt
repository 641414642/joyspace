package com.unicolour.joyspace.controller.graphql

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.service.GraphQLService
import graphql.GraphQL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import java.util.HashMap




@RestController
class GraphQLController {
    @Autowired
    lateinit var graphQLService: GraphQLService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @RequestMapping("/graphql", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun graphQLQuery(request: HttpServletRequest) : Any {
        val schema = graphQLService.getGraphQLSchema()

        val graphQL = GraphQL.newGraphQL(schema)
//                .queryExecutionStrategy(ExecutorServiceExecutionStrategy(threadPoolExecutor))
//                .mutationExecutionStrategy(SimpleExecutionStrategy())
                .build()

        val query = request.getParameter("query")

        var operationName: String? = request.getParameter("operationName")
        if (operationName != null && operationName.isBlank()) {
            operationName = null
        }

        val variables = request.getParameter("variables")
        var arguments: Map<String, Any>? = emptyMap();
        if (!variables.isNullOrBlank()) {
            val typeRef = object : TypeReference<HashMap<String, Any>>() {}
            arguments = objectMapper.readValue(variables, typeRef)
        }

        val result = graphQL.execute(query, operationName, null, arguments)
        return result
    }

    @RequestMapping("/graphql",
            method = arrayOf(RequestMethod.POST),
            consumes = arrayOf(APPLICATION_JSON_VALUE, "application/graphql"))
    @ResponseBody
    fun graphQLMutation(request: HttpServletRequest, @RequestBody graphQLRequest: GraphQLRequest?) : Any {
        val schema = graphQLService.getGraphQLSchema()

        val graphQL = GraphQL.newGraphQL(schema)
//                .queryExecutionStrategy(ExecutorServiceExecutionStrategy(threadPoolExecutor))
//                .mutationExecutionStrategy(SimpleExecutionStrategy())
                .build()

        var query = request.getParameter("query")
        if (query.isNullOrBlank()) {
            query = if (graphQLRequest == null) "" else graphQLRequest.query
        }

        var operationName:String? = if (graphQLRequest == null) null else graphQLRequest.operationName
        if (operationName != null && operationName.isBlank()) {
            operationName = null
        }

        val arguments: Map<String, Any>? = if (graphQLRequest == null) emptyMap() else graphQLRequest.variables

        val result = graphQL.execute(query, operationName, null, arguments)

        return result
    }
}

data class GraphQLRequest(
    var query: String = "",
    var operationName: String? = "",
    var variables: Map<String, Any>? = emptyMap()
)

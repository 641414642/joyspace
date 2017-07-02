package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.service.GraphQLService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.service.UserService
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets.UTF_8


@Service
class GraphQLServiceImpl : GraphQLService {
    @Autowired
    lateinit var printStationService: PrintStationService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var appContext: ApplicationContext

    private var graphQLSchema: GraphQLSchema? = null

    @Synchronized
    override fun getGraphQLSchema(): GraphQLSchema? {
        if (graphQLSchema == null) {
            val schemaParser = SchemaParser()
            val schemaGenerator = SchemaGenerator()

            val resource = appContext.getResource("classpath:joyspace.graphql")
            val schemaStr = resource.url.readText(UTF_8)

            val typeRegistry = schemaParser.parse(schemaStr)
            val wiring = buildRuntimeWiring()

            graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring)
        }

        return graphQLSchema
    }

    fun buildRuntimeWiring(): RuntimeWiring {
        return RuntimeWiring.newRuntimeWiring()
                .type("QueryType", { typeWiring ->
                    typeWiring.dataFetcher("printStation", printStationService.getPrintStationDataFetcher())
                })
                .type("MutationType", { typeWiring ->
                    typeWiring.dataFetcher("login", userService.getLoginDataFetcher())
                    typeWiring.dataFetcher("sendRegVerifyCode", userService.getSendRegVerifyCodeDataFetcher())
                    typeWiring.dataFetcher("userRegister", userService.getUserRegisterDataFetcher())
                })
                .type("PrintStation", { typeWiring ->
                    typeWiring.dataFetchers(printStationService.getDataFetchers())
                })
                .type("Product", { typeWiring ->
                    typeWiring.dataFetcher("type", productService.getDataFetcher("type"))
                })
                .type("LoginUser", { typeWiring ->
                    typeWiring.dataFetcher("authToken", userService.getAuthTokenDataFetcher())
                })
                .build()
    }
}
package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dto.GraphQLRequestResult
import com.unicolour.joyspace.model.PrintOrderState
import com.unicolour.joyspace.service.*
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
    lateinit var printOrderService: PrintOrderService

    @Autowired
    lateinit var imageService: ImageService

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
                    typeWiring.dataFetcher("getPrintOrder", printOrderService.getPrintOrderDataFetcher())
                })
                .type("MutationType", { typeWiring ->
                    typeWiring.dataFetcher("login", userService.loginDataFetcher)
                    typeWiring.dataFetcher("sendRegVerifyCode", userService.sendRegVerifyCodeDataFetcher)
                    typeWiring.dataFetcher("userRegister", userService.userRegisterDataFetcher)
                    typeWiring.dataFetcher("requestResetPassword", userService.requestResetPasswordDataFetcher)
                    typeWiring.dataFetcher("resetPassword", userService.resetPasswordDataFetcher)
                    typeWiring.dataFetcher("printOrderDownloaded", printOrderService.getUpdateOrderStateDataFetcher(PrintOrderState.DOWNLOADED))
                    typeWiring.dataFetcher("printOrderPrinted", printOrderService.getUpdateOrderStateDataFetcher(PrintOrderState.PRINTED))
                })
                .type("RequestResult", { typeWiring ->
                    typeWiring.dataFetcher("description", { environment ->
                        val result = environment.getSource<GraphQLRequestResult>()
                        val language = environment.getArgument<String>("language")
                        when (language) {
                            "zh" -> result.resultCode.desc
                            else -> result.resultCode.descEn
                        }
                    })
                })
                .type("PrintStation", { typeWiring ->
                    typeWiring.dataFetchers(printStationService.getDataFetchers())
                })
                .type("Product", { typeWiring ->
                    typeWiring.dataFetcher("type", productService.getDataFetcher("type"))
                    typeWiring.dataFetcher("width", productService.getDataFetcher("width"))
                    typeWiring.dataFetcher("height", productService.getDataFetcher("height"))
                    typeWiring.dataFetcher("imageRequired", productService.getDataFetcher("imageRequired"))
                    typeWiring.dataFetcher("thumbnailUrl", productService.getDataFetcher("thumbnailUrl"))
                    typeWiring.dataFetcher("previewUrl", productService.getDataFetcher("previewUrl"))
                    typeWiring.dataFetcher("userImages", productService.getDataFetcher("userImages"))
                })
                .type("PrintOrderItem", { typeWiring ->
                    typeWiring.dataFetcher("imageFiles", printOrderService.getImageFilesDataFetcher())
                })
                .type("UserImageFile", { typeWiring ->
                    typeWiring.dataFetcher("url", imageService.getImageFileUrlDataFetcher())
                })
                .build()
    }
}
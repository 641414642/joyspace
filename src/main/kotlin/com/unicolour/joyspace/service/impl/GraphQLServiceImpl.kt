package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dto.GraphQLRequestResult
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
    lateinit var templateService: TemplateService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var printOrderService: PrintOrderService

    @Autowired
    lateinit var imageService: ImageService

    @Autowired
    lateinit var appContext: ApplicationContext

    @Autowired
    lateinit var couponService: CouponService

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
                    typeWiring.dataFetcher("printStation", printStationService.printStationDataFetcher)
                    typeWiring.dataFetcher("findPrintStationsByDistance", printStationService.byDistanceDataFetcher)
                    typeWiring.dataFetcher("findPrintStationsByCity", printStationService.byCityDataFetcher)
                    typeWiring.dataFetcher("findNearestPrintStation", printStationService.nearestDataFetcher)
                    typeWiring.dataFetcher("userCouponList", couponService.userCouponListDataFetcher)
                    typeWiring.dataFetcher("getPrintOrder", printOrderService.printOrderDataFetcher)
                    typeWiring.dataFetcher("getTemplateFileUrl", templateService.templateFileUrlDataFetcher)
                    typeWiring.dataFetcher("templates", templateService.templatesDataFetcher)
                })
                .type("MutationType", { typeWiring ->
                    typeWiring.dataFetcher("login", userService.loginDataFetcher)
                    typeWiring.dataFetcher("sendVerifyCode", userService.sendVerifyCodeDataFetcher)
                    typeWiring.dataFetcher("userRegister", userService.userRegisterDataFetcher)
                    typeWiring.dataFetcher("resetPassword", userService.resetPasswordDataFetcher)
                    typeWiring.dataFetcher("claimCoupon", couponService.claimCouponDataFetcher)
                    typeWiring.dataFetcher("printStationLogin", printStationService.loginDataFetcher)
                    typeWiring.dataFetcher("printOrderDownloaded", printOrderService.printerOrderDownloadedDataFetcher)
                    typeWiring.dataFetcher("printOrderPrinted", printOrderService.printerOrderPrintedDataFetcher)
                })
                .type("RequestResult", { typeWiring ->
                    typeWiring.dataFetcher("description", { environment ->
                        val result = environment.getSource<GraphQLRequestResult>()
                        val language = environment.getArgument<String>("language")
                        when (language) {
                            "en" -> result.resultCode.descEn
                            else -> result.resultCode.desc
                        }
                    })
                })
                .type("Product", { typeWiring ->
                    typeWiring.dataFetcher("type", productService.getDataFetcher("type"))
                    typeWiring.dataFetcher("typeInt", productService.getDataFetcher("typeInt"))
                    typeWiring.dataFetcher("templateWidth", productService.getDataFetcher("templateWidth"))
                    typeWiring.dataFetcher("templateHeight", productService.getDataFetcher("templateHeight"))
                    typeWiring.dataFetcher("width", productService.getDataFetcher("width"))
                    typeWiring.dataFetcher("height", productService.getDataFetcher("height"))
                    typeWiring.dataFetcher("displaySize", productService.getDataFetcher("displaySize"))
                    typeWiring.dataFetcher("idPhotoMaskImageUrl", productService.getDataFetcher("idPhotoMaskImageUrl"))
                    typeWiring.dataFetcher("imageRequired", productService.getDataFetcher("imageRequired"))
                    typeWiring.dataFetcher("thumbnailImageUrl", productService.getDataFetcher("thumbnailImageUrl"))
                    typeWiring.dataFetcher("previewImageUrls", productService.getDataFetcher("previewImageUrls"))
                    typeWiring.dataFetcher("templateImages", productService.getDataFetcher("templateImages"))
                    typeWiring.dataFetcher("price", productService.getDataFetcher("price"))
                    typeWiring.dataFetcher("remark", productService.getDataFetcher("remark"))
                    typeWiring.dataFetcher("id", productService.getDataFetcher("id"))
                    typeWiring.dataFetcher("name", productService.getDataFetcher("name"))
                    typeWiring.dataFetcher("version", productService.getDataFetcher("version"))
                    typeWiring.dataFetcher("templateUrl", productService.getDataFetcher("templateUrl"))
                })
                .type("TemplateImage", { typeWiring ->
                    typeWiring.dataFetcher("url", templateService.getTemplateImageDataFetcher("url"))
                })
                .type("PrintStation", { typeWiring ->
                    typeWiring.dataFetcher("name", printStationService.getDataFetcher("name"))
                    typeWiring.dataFetcher("address", printStationService.getDataFetcher("address"))
                    typeWiring.dataFetcher("latitude", printStationService.getDataFetcher("latitude"))
                    typeWiring.dataFetcher("longitude", printStationService.getDataFetcher("longitude"))
                    typeWiring.dataFetcher("images", printStationService.getDataFetcher("images"))
                    typeWiring.dataFetcher("transportation", printStationService.getDataFetcher("transportation"))
                    typeWiring.dataFetcher("distance", printStationService.getDataFetcher("distance"))
                    typeWiring.dataFetcher("products", printStationService.getDataFetcher("products"))
                })
                .type("PrintOrderItem", { typeWiring ->
                    typeWiring.dataFetcher("imageFiles", printOrderService.imageFilesDataFetcher)
                })
                .type("UserImageFile", { typeWiring ->
                    typeWiring.dataFetcher("url", imageService.getImageFileUrlDataFetcher())
                })
                .build()
    }
}
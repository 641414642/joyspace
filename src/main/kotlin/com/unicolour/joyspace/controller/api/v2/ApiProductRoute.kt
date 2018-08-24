package com.unicolour.joyspace.controller.api.v2

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.SceneDao
import com.unicolour.joyspace.dao.TemplateImageInfoDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.dto.ProductType
import com.unicolour.joyspace.dto.Scene
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.TemplateService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.math.BigDecimal

@RestController
class ApiProductRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var productDao: ProductDao
    @Autowired
    private lateinit var templateImageInfoDao: TemplateImageInfoDao
    @Autowired
    private lateinit var templateService: TemplateService
    @Autowired
    private lateinit var printStationProductDao: PrintStationProductDao
    @Autowired
    private lateinit var sceneDao: SceneDao
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Value("\${com.unicolour.joyspace.baseUrl}")
    private lateinit var baseUrl: String
    @Value("\${com.unicolour.joyspace.assetsDir}")
    private lateinit var assetsDir: String


    /**
     * 主页数据
     */
    @GetMapping(value = "/v2/app/homepage")
    fun showHomePage(@RequestParam(required = false, value = "printStationId", defaultValue = "1") printStationId: Int): RestResponse {
        val advers = mutableListOf<Advert>()
        advers.add(Advert("ad_1", "轮播图", "", "$baseUrl/doc/home_page/1.png"))
        advers.add(Advert("ad_2", "轮播图", "", "$baseUrl/doc/home_page/2.png"))
        advers.add(Advert("ad_3", "轮播图", "", "$baseUrl/doc/home_page/3.png"))
        advers.add(Advert("ad_4", "轮播图", "", "$baseUrl/doc/home_page/4.png"))
        advers.add(Advert("ad_5", "轮播图", "", "$baseUrl/doc/home_page/5.png"))
        val producTypes = mutableListOf<ProductType>()
        if (beSupportProductType(com.unicolour.joyspace.model.ProductType.PHOTO, printStationId)) {
            producTypes.add(ProductType(0, "普通照片", "智能手机照片高质量打印", "$baseUrl/doc/home_page/product_type_0.png"))
        }
        if (beSupportProductType(com.unicolour.joyspace.model.ProductType.ID_PHOTO, printStationId)) {
            producTypes.add(ProductType(1, "证件照", "支持多种尺寸，自动排版", "$baseUrl/doc/home_page/product_type_1.png"))
        }
        if (beSupportProductType(com.unicolour.joyspace.model.ProductType.TEMPLATE, printStationId)) {
            producTypes.add(ProductType(2, "模版", "多种精美模板 随心定制", "$baseUrl/doc/home_page/product_type_2.png"))
        }
        if (beSupportProductType(com.unicolour.joyspace.model.ProductType.ALBUM, printStationId)) {
            producTypes.add(ProductType(3, "相册", "生活也许是一本书", "$baseUrl/doc/home_page/product_type_3.png"))
        }
        if (beSupportProductType(com.unicolour.joyspace.model.ProductType.DIY, printStationId)) {
            producTypes.add(ProductType(5, "定制产品", "DIY的T恤&丝巾", "$baseUrl/doc/home_page/product_type_5.png"))
        }
        val homePage = HomePageVo(advers, producTypes)
        return RestResponse.ok(homePage)
    }

    private fun beSupportProductType(type: com.unicolour.joyspace.model.ProductType, printStationId: Int): Boolean {
        val templateIds = templateService.queryTemplates(type, "", true, "id asc").map { it.id }
        val products = productDao.findByTemplateIdInAndDeletedOrderBySequence(templateIds, false)
        products.asSequence().firstOrNull {
            printStationProductDao.existsByPrintStationIdAndProductId(printStationId, it.id)
        }?.let { return true } ?: return false
    }


    /**
     * 获取某个类型的全部产品（规格／模版）信息
     */
    @GetMapping(value = "/v2/product/{type}")
    fun getProductsByType(@PathVariable("type") type: Int,
                          @RequestParam(required = false, value = "printStationId") printStationId: Int?): RestResponse {
        val productType = com.unicolour.joyspace.model.ProductType.values().firstOrNull { it.value == type }
        val templateIds = templateService.queryTemplates(productType, "", true, "id asc").map { it.id }
        var products = productDao.findByTemplateIdInAndDeletedOrderBySequence(templateIds, false)
        products = if (printStationId != null) {
            products.filter {
                printStationProductDao.existsByPrintStationIdAndProductId(printStationId, it.id)
            }
        } else {
            products.filter {
                printStationProductDao.existsByPrintStationIdAndProductId(1, it.id)
            }
        }
        val productVoList = products.map {
            val tpl = it.template
            val w = tpl.width
            val h = tpl.height
            var displaySize = String.format("%1$.0f x %2$.0f mm", w, h)
            if (tpl.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value) {
                val templateImage = templateImageInfoDao.findByTemplateIdAndTemplateVersion(tpl.id, tpl.currentVersion).first()
                displaySize = String.format("%1$.0f x %2$.0f mm", templateImage.width, templateImage.height)
            }
            var thumbnailImageUrl = it.imageFiles
                    .filter { it.type == ProductImageFileType.THUMB.value }
                    .map { "$baseUrl/assets/product/images/${it.id}.${it.fileType}" }
                    .firstOrNull()
            if (thumbnailImageUrl.isNullOrEmpty()) {
                if (tpl.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value || tpl.type == com.unicolour.joyspace.model.ProductType.PHOTO.value) {
                    val thumbFile = File(assetsDir, "template/preview/${tpl.id}_v${tpl.currentVersion}/thumb.jpg")
                    if (thumbFile.exists()) thumbnailImageUrl = "$baseUrl/assets/template/preview/${tpl.id}_v${tpl.currentVersion}/thumb.jpg"
                }
            }
            var mode = 240
            if (it.template.width * it.template.height > 19354.8) mode = 180
            mode = 360
            ProductVo(it.id,
                    it.name,
                    getPixels(it.template.width, mode),
                    getPixels(it.template.height, mode),
                    it.template.type,
                    com.unicolour.joyspace.model.ProductType.values().first { it.value == tpl.type }.dispName,
                    tpl.currentVersion,
                    displaySize,
                    tpl.minImageCount,
                    it.remark,
                    it.defaultPrice,
                    thumbnailImageUrl)
        }

        return if (productType == com.unicolour.joyspace.model.ProductType.DIY) {
            RestResponse.ok(getDiyProductRes(productVoList))
        } else {
            RestResponse.ok(productVoList)
        }
    }

    private fun getDiyProductRes(productVos: List<ProductVo>): List<DiyProductVo> {
        val tShirt = DiyProductVo().apply {
            this.name = "T恤"
            this.thumbnailImageUrl = "$baseUrl/doc/home_page/tx.png"
            this.styles = listOf(
                    Style().apply {
                        name = "短袖T恤 浅色男款"
                        sizes = listOf(
                                StyleSize().apply {
                                    name = "XS"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 XS" }
                                },
                                StyleSize().apply {
                                    name = "S"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 S" }
                                },
                                StyleSize().apply {
                                    name = "M"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 M" }
                                },
                                StyleSize().apply {
                                    name = "L"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 L" }
                                },
                                StyleSize().apply {
                                    name = "XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 XL" }
                                },
                                StyleSize().apply {
                                    name = "2XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 2XL" }
                                },
                                StyleSize().apply {
                                    name = "3XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 3XL" }
                                },
                                StyleSize().apply {
                                    name = "4XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 白色男款 4XL" }
                                }
                        ).filter { it.products.isNotEmpty() }
                    },
                    Style().apply {
                        name = "短袖T恤 浅色女款"
                        sizes = listOf(
                                StyleSize().apply {
                                    name = "S"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 白色女款 S" }
                                },
                                StyleSize().apply {
                                    name = "M"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 白色女款 M" }
                                },
                                StyleSize().apply {
                                    name = "L"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 白色女款 L" }
                                },
                                StyleSize().apply {
                                    name = "XL"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 白色女款 XL" }
                                },
                                StyleSize().apply {
                                    name = "2XL"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 白色女款 2XL" }
                                },
                                StyleSize().apply {
                                    name = "3XL"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 白色女款 3XL" }
                                }
                        ).filter { it.products.isNotEmpty() }
                    },
                    Style().apply {
                        name = "短袖T恤 深色男款"
                        sizes = listOf(
                                StyleSize().apply {
                                    name = "XS"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 XS" }
                                },
                                StyleSize().apply {
                                    name = "S"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 S" }
                                },
                                StyleSize().apply {
                                    name = "M"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 M" }
                                },
                                StyleSize().apply {
                                    name = "L"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 L" }
                                },
                                StyleSize().apply {
                                    name = "XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 XL" }
                                },
                                StyleSize().apply {
                                    name = "2XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 2XL" }
                                },
                                StyleSize().apply {
                                    name = "3XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 3XL" }
                                },
                                StyleSize().apply {
                                    name = "4XL"
                                    gender = "男"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色男款 4XL" }
                                }
                        ).filter { it.products.isNotEmpty() }
                    },
                    Style().apply {
                        name = "短袖T恤 深色女款"
                        sizes = listOf(
                                StyleSize().apply {
                                    name = "S"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色女款 S" }
                                },
                                StyleSize().apply {
                                    name = "M"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色女款 M" }
                                },
                                StyleSize().apply {
                                    name = "L"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色女款 L" }
                                },
                                StyleSize().apply {
                                    name = "XL"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色女款 XL" }
                                },
                                StyleSize().apply {
                                    name = "2XL"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色女款 2XL" }
                                },
                                StyleSize().apply {
                                    name = "3XL"
                                    gender = "女"
                                    products = productVos.filter { it.remark == "短袖T恤 黑色女款 3XL" }
                                }
                        ).filter { it.products.isNotEmpty() }
                    }).filter { it.sizes!!.isNotEmpty() }

        }
        val scarf = DiyProductVo().apply {
            name = "丝巾"
            this.thumbnailImageUrl = "$baseUrl/doc/home_page/sj.png"
            styles = listOf(
                    Style().apply {
                        name = "丝巾（水平）"
                        product = productVos.firstOrNull { it.remark == name }
                    },
                    Style().apply {
                        name = "丝巾（垂直）"
                        product = productVos.firstOrNull { it.remark == name }
                    }
            ).filter { it.product != null }
        }
        return listOf(tShirt, scarf).filter { it.styles!!.isNotEmpty() }
    }


    /**
     * 获取某个产品（规格／模版）的详细信息
     */
    @GetMapping(value = "/v2/product/detail/{id}")
    fun getTemplateDetail(@PathVariable("id") id: Int): RestResponse {
        val product = productDao.findOne(id)
        val template = product.template

        val templateVo = if (template.type == com.unicolour.joyspace.model.ProductType.ALBUM.value || template.type == com.unicolour.joyspace.model.ProductType.DIY.value) {
            val sceneList = sceneDao.findByAlbumIdAndDeletedOrderByIndex(template.id, false).map {
                val scene = getScene(it.template).second
                scene.name = it.name
                scene.index = it.index
                scene.id = it.id
                scene
            }
            TemplateVo(template.id, template.currentVersion, template.name, template.type, sceneList)
        } else {
            val pair = getScene(template)
            val idPhotoMaskImageUrl = pair.first
            val scene = pair.second
            TemplateVo(template.id, template.currentVersion, template.name, template.type, listOf(scene), idPhotoMaskImageUrl)
        }
        return RestResponse.ok(templateVo)
    }

    /**
     * 获取某个产品（规格／模版）的详细信息
     */
    @GetMapping(value = "/v2/product/detailInMM/{id}")
    fun getTemplateDetailInMM(@PathVariable("id") id: Int): RestResponse {
        val product = productDao.findOne(id)
        val template = product.template
        val templateVo = if (template.type == com.unicolour.joyspace.model.ProductType.ALBUM.value || template.type == com.unicolour.joyspace.model.ProductType.DIY.value) {
            val sceneList = sceneDao.findByAlbumIdAndDeletedOrderByIndex(template.id, false).map {
                val scene = getSceneInMM(it.template)
                scene.name = it.name
                scene.index = it.index
                scene.id = it.id
                scene
            }
            TemplateVo(template.id, template.currentVersion, template.name, template.type, sceneList)
        } else {
            val scene = getSceneInMM(template)
            TemplateVo(template.id, template.currentVersion, template.name, template.type, listOf(scene))
        }
        return RestResponse.ok(templateVo)
    }

    private fun getScene(template: Template): Pair<String, Scene> {
        val layerBg = Layer(1, LayerType.BACKGROUND.name.toLowerCase())
        val layerUser = Layer(2, LayerType.IMAGE.name.toLowerCase())
        val layerFront = Layer(3, LayerType.FRONT.name.toLowerCase())
        val layerControl = Layer(4, LayerType.CONTROL.name.toLowerCase())
        var idPhotoMaskImageUrl = ""
//        val mode = if (template.width * template.height > 19354.8) 180 else 240
        val mode = 360
        if (template.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value) {
            idPhotoMaskImageUrl = "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/mask.png"
            layerBg.images.add(
                    Img(id = 1,
                            type = TemplateImageType.STICKER.name.toLowerCase(),
                            x = 0.0,
                            y = 0.0,
                            width = getPixels(template.width, mode),
                            height = getPixels(template.height, mode),
                            resourceURL = "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/template.jpg"
                    )
            )
        }
        val templateImages = templateImageInfoDao.findByTemplateIdAndTemplateVersion(template.id, template.currentVersion)
        templateImages.sortedBy { it.id }.forEach { templateImage ->
            val image = Img(id = templateImage.id,
                    type = TemplateImageType.values().first { it.value == templateImage.type }.name.toLowerCase(),
                    x = getPixels(templateImage.x, mode),
                    y = getPixels(templateImage.y, mode),
                    width = getPixels(templateImage.width, mode),
                    height = getPixels(templateImage.height, mode),
                    angleClip = templateImage.angleClip,
                    resourceURL = getThumbURl(templateImage, template)
            )
            when (templateImage.layerType) {
                LayerType.BACKGROUND.value -> layerBg.images.add(image)
                LayerType.IMAGE.value -> layerUser.images.add(image)
                LayerType.FRONT.value -> layerFront.images.add(image)
                LayerType.CONTROL.value -> layerControl.images.add(image)
            }
        }
        val scene = Scene(
                id = template.id,
                name = "",
                type = "page",
                width = getPixels(template.width, mode),
                height = getPixels(template.height, mode),
                layers = listOf(layerBg, layerUser, layerFront, layerControl)
        )
        return Pair(idPhotoMaskImageUrl, scene)
    }

    /**
     * 毫米->像素   240DPI    if 宽x高>=19354.8mm use 180DPI
     * @param mode 分辨率
     */
    private fun getPixels(mm: Double, mode: Int): Double {
        return BigDecimal(mm).divide(BigDecimal(25.4), 7, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal(mode)).setScale(0, BigDecimal.ROUND_HALF_UP).toDouble()
    }


    private fun getSceneInMM(template: Template): Scene {
        val layerBg = Layer(1, "background")
        val layerUser = Layer(2, "image")
        val layerFront = Layer(3, "front")
        val layerControl = Layer(4, "control")
        if (template.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value) {
            layerBg.images.add(
                    Img(id = 1,
                            type = TemplateImageType.STICKER.name.toLowerCase(),
                            x = 0.0,
                            y = 0.0,
                            width = template.width,
                            height = template.height,
                            resourceURL = "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/template.jpg"
                    )
            )
        }
        val templateImages = templateImageInfoDao.findByTemplateIdAndTemplateVersion(template.id, template.currentVersion)
        templateImages.sortedBy { it.id }.forEach { templateImage ->
            val image = Img(id = templateImage.id,
                    type = TemplateImageType.values().first { it.value == templateImage.type }.name.toLowerCase(),
                    x = templateImage.x,
                    y = templateImage.y,
                    width = templateImage.width,
                    height = templateImage.height,
                    angleClip = templateImage.angleClip,
                    resourceURL = if (templateImage.href.isNullOrEmpty()) "" else "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/${templateImage.href}"
            )
            when (templateImage.layerType) {
                LayerType.BACKGROUND.value -> layerBg.images.add(image)
                LayerType.IMAGE.value -> layerUser.images.add(image)
                LayerType.FRONT.value -> layerFront.images.add(image)
                LayerType.CONTROL.value -> layerControl.images.add(image)
            }
        }
        val scene = Scene(
                id = template.id,
                name = "",
                type = "page",
                width = template.width,
                height = template.height,
                layers = listOf(layerBg, layerUser, layerFront, layerFront))
        return scene
    }

    private fun getThumbURl(templateImage: TemplateImageInfo, template: Template): String {

        if (templateImage.href.isNullOrEmpty()) {
            return ""
        } else {
            val thumbFile = File(assetsDir, "template/preview/${template.id}_v${template.currentVersion}/${templateImage.href!!.replace("/", "/thum_")}")
            if (thumbFile.exists()) return "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/${templateImage.href!!.replace("/", "/thum_")}"
            return "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/${templateImage.href}"
        }
    }
}



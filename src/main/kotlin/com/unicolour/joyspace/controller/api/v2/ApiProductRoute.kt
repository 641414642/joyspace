package com.unicolour.joyspace.controller.api.v2

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.TemplateDao
import com.unicolour.joyspace.dao.TemplateImageInfoDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.ProductImageFileType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ApiProductRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var productDao: ProductDao
    @Autowired
    private lateinit var templateImageInfoDao: TemplateImageInfoDao
    @Autowired
    private lateinit var templateDao: TemplateDao
    @Autowired
    private lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Value("\${com.unicolour.joyspace.baseUrl}")
    private lateinit var baseUrl: String
    @Value("classpath:static/doc/home_page/9526/test.json")
    private lateinit var json_9526: Resource
    @Value("classpath:static/doc/home_page/9527/test.json")
    private lateinit var json_9527: Resource
    @Value("classpath:static/doc/home_page/9528/test.json")
    private lateinit var json_9528: Resource
    @Value("classpath:static/doc/home_page/9529/test.json")
    private lateinit var json_9529: Resource
    @Value("classpath:static/doc/home_page/9528/test_mm.json")
    private lateinit var json_9528_mm: Resource



    /**
     * 获取证件照参数
     */
    @GetMapping(value = "/v2/photo/param")
    fun getIdPhotoParam(@RequestParam("rowCount") rowCount: Int,
                        @RequestParam("columnCount") columnCount: Int,
                        @RequestParam("name") name: String): RestResponse {
        val idPhotoParam = IDPhotoParam()
        return RestResponse.ok(idPhotoParam)
    }



    /**
     * 主页数据
     */
    @GetMapping(value = "/v2/app/homepage")
    fun showHomePage(): RestResponse {
        val advers = mutableListOf<Advert>()
        advers.add(Advert("ad_1", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/1.png"))
        advers.add(Advert("ad_2", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/4.png"))
        advers.add(Advert("ad_3", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/3.png"))
        advers.add(Advert("ad_4", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/2.png"))
        val producTypes = mutableListOf<ProductType>()
        producTypes.add(ProductType(0, "普通照片", "智能手机照片高质量打印","https://joyspace1.uni-colour.com/doc/home_page/product_type_0.png"))
        producTypes.add(ProductType(1, "证件照", "支持多种尺寸，自动排版","https://joyspace1.uni-colour.com/doc/home_page/product_type_1.png"))
        producTypes.add(ProductType(2, "模版拼图", "多种精美模板 随心定制","https://joyspace1.uni-colour.com/doc/home_page/product_type_2.png"))
        //producTypes.add(ProductType(3, "相册", "生活也许是一本书","https://joyspace1.uni-colour.com/doc/home_page/product_type_3.png"))
        val homePage = HomePageVo(advers, producTypes)
        return RestResponse.ok(homePage)
    }


    /**
     * 获取某个类型的全部产品（规格／模版）信息
     */
    @GetMapping(value = "/v2/product/{type}")
    fun getProductsByType(@PathVariable("type") type: Int,
                          @RequestParam(required = false, value = "printStationId") printStationId: Int?): RestResponse {
        if (type == 2) {
            val products = mutableListOf<ProductVo>()
            products.add(ProductVo(9526,
                    "展会6",
                    960.0,
                    1440.0,
                    2,
                    "模板拼图",
                    1,
                    "101.6 x 152.4 mm",
                    1,
                    "",
                    100,
                    null))
            products.add(ProductVo(9527,
                    "展会7",
                    960.0,
                    1440.0,
                    2,
                    "模板拼图",
                    1,
                    "101.6 x 152.4 mm",
                    1,
                    "",
                    100,
                    null))
            products.add(ProductVo(9528,
                    "展会8",
                    1440.0,
                    960.0,
                    2,
                    "模板拼图",
                    1,
                    "152.4 x 101.6 mm",
                    1,
                    "",
                    100,
                    null))
            products.add(ProductVo(9529,
                    "展会9",
                    960.0,
                    1440.0,
                    2,
                    "模板拼图",
                    1,
                    "101.6 x 152.4 mm",
                    1,
                    "",
                    100,
                    null))
            return RestResponse.ok(products)
        }
        val templateIds = templateDao.findByType(type).map { it.id }
        var products = productDao.findByTemplateIdInAndDeletedOrderBySequence(templateIds, false)
        if (printStationId != null) {
            products = products.filter {
                printStationProductDao.existsByPrintStationIdAndProductId(printStationId, it.id)
            }
        }
        val productVoList = products.map {
            val tpl = it.template
            val w = tpl.width
            val h = tpl.height
            var displaySize = String.format("%1$.0f x %2$.0f mm", w, h)
            if (tpl.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value){
                val templateImage = templateImageInfoDao.findByTemplateIdAndTemplateVersion(tpl.id, tpl.currentVersion).first()
                displaySize = String.format("%1$.0f x %2$.0f mm", templateImage.width, templateImage.height)
            }
            val thumbnailImageUrl = it.imageFiles
                    .filter { it.type == ProductImageFileType.THUMB.value }
                    .map { "$baseUrl/assets/product/images/${it.id}.${it.fileType}" }
                    .firstOrNull()
            var mode = 240
            if (it.template.width*it.template.height>19354.8) mode = 180
            ProductVo(it.id,
                    it.name,
                    getPixels(it.template.width,mode),
                    getPixels(it.template.height,mode),
                    it.template.type,
                    com.unicolour.joyspace.model.ProductType.values().first { it.value == tpl.type }.dispName,
                    tpl.currentVersion,
                    displaySize,
                    tpl.minImageCount,
                    it.remark,
                    it.defaultPrice,
                    thumbnailImageUrl)
        }
        return RestResponse.ok(productVoList)
    }

    /**
     * 获取某个产品（规格／模版）的详细信息
     */
    @GetMapping(value = "/v2/product/detail/{id}")
    fun getTemplateDetail(@PathVariable("id") id: Int): RestResponse {
        val testProductList = listOf(9526, 9527, 9528, 9529)
        if (id in testProductList) {
            when (id) {
                9526 -> return RestResponse.ok(objectMapper.readValue(json_9526.inputStream, TemplateVo::class.java))
                9527 -> return RestResponse.ok(objectMapper.readValue(json_9527.inputStream, TemplateVo::class.java))
                9528 -> return RestResponse.ok(objectMapper.readValue(json_9528.inputStream, TemplateVo::class.java))
                9529 -> return RestResponse.ok(objectMapper.readValue(json_9529.inputStream, TemplateVo::class.java))
            }
        }

        val product = productDao.findOne(id)
        val template = product.template

        var mode = 240
        if (template.width * template.height > 19354.8) {
            mode = 180
        }

        val layerBg = Layer(1, "background")
        if (template.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value) {
            layerBg.images = listOf(Img(1, "sticker", 0.0, 0.0, getPixels(template.width,mode), getPixels(template.height,mode), 0.0, "", "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/template.jpg"))
        }

        val layerUser = Layer(2, "image")
        val templateImages = templateImageInfoDao.findByTemplateIdAndTemplateVersion(template.id, template.currentVersion)
        layerUser.images = templateImages.map {
            var mode = 240
            if (it.width*it.height>19354.8) mode = 180
            Img(it.id,
                    "user",
                    getPixels(it.x, mode),
                    getPixels(it.y, mode),
                    getPixels(it.width, mode),
                    getPixels(it.height, mode),
                    0.0,
                    "",
                    "")
        }

        val scene = Scene(1, "", "page", getPixels(template.width,mode), getPixels(template.height,mode), layers = listOf(layerBg, layerUser))
        val templateVo = TemplateVo(template.id, template.currentVersion, template.name, template.type, listOf(scene))

        return RestResponse.ok(templateVo)
    }

    /**
     * 毫米->像素   240DPI    if 宽x高>=19354.8mm use 180DPI
     * @param mode 分辨率
     */
    private fun getPixels(mm: Double,mode:Int): Double {
        return BigDecimal(mm).divide(BigDecimal(25.4),7,BigDecimal.ROUND_HALF_UP).multiply(BigDecimal(mode)).setScale(0,BigDecimal.ROUND_HALF_UP).toDouble()
    }

    /**
     * 获取某个产品（规格／模版）的详细信息
     */
    @GetMapping(value = "/v2/product/detailInMM/{id}")
    fun getTemplateDetailInMM(@PathVariable("id") id: Int): RestResponse {
        if (id==9528) return RestResponse.ok(objectMapper.readValue(json_9528_mm.inputStream, TemplateVo::class.java))
        val product = productDao.findOne(id)
        val template = product.template

        val layerBg = Layer(1, "background")
        if (template.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value) {
            layerBg.images = listOf(
                Img(id = 1,
                    type = "sticker",
                    x = 0.0,
                    y = 0.0,
                    width = template.width,
                    height = template.height,
                    resourceURL = "$baseUrl/assets/template/preview/${template.id}_v${template.currentVersion}/template.jpg"
                )
            )
        }

        val layerUser = Layer(2, "image")
        val templateImages = templateImageInfoDao.findByTemplateIdAndTemplateVersion(template.id, template.currentVersion)
        layerUser.images = templateImages.map {
            Img(id = it.id,
                type = "user",
                x = it.x,
                y = it.y,
                width = it.width,
                height = it.height
            )
        }

        val scene = Scene(
                id = 1,
                name = "",
                type = "page",
                width = template.width,
                height = template.height,
                layers = listOf(layerBg, layerUser))

        val templateVo = TemplateVo(template.id, template.currentVersion, template.name, template.type, listOf(scene))

        return RestResponse.ok(templateVo)
    }
}


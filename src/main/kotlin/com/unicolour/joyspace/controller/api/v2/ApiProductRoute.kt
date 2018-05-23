package com.unicolour.joyspace.controller.api.v2

import com.google.gson.Gson
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.TemplateDao
import com.unicolour.joyspace.dao.TemplateImageInfoDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.ProductImageFileType
import com.unicolour.joyspace.service.AdSetService
import com.unicolour.joyspace.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ApiProductRoute {
    val logger = LoggerFactory.getLogger(this::class.java)
    @Autowired
    private lateinit var adSetService: AdSetService
    @Autowired
    private lateinit var productService: ProductService
    @Autowired
    private lateinit var productDao: ProductDao
    @Autowired
    private lateinit var templateImageInfoDao: TemplateImageInfoDao
    @Autowired
    private lateinit var templateDao: TemplateDao
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

    /**
     * 主页数据
     */
    @GetMapping(value = "/v2/app/homepage")
    fun showHomePage(): RestResponse {
        val advers = mutableListOf<Advert>()
        advers.add(Advert("ad_1", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/1.png"))
        advers.add(Advert("ad_2", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/2.png"))
        advers.add(Advert("ad_3", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/3.png"))
        advers.add(Advert("ad_4", "轮播图", "", "https://joyspace1.uni-colour.com/doc/home_page/4.png"))
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
    fun getProductsByType(@PathVariable("type") type: Int): RestResponse {
        if (type == 2) {
            val products = mutableListOf<ProductVo>()
            products.add(ProductVo(9526,
                    "展会6",
                    1440.0,
                    2160.0,
                    2,
                    "模板拼图",
                    1,
                    "1400 x 2160 mm",
                    1,
                    "",
                    100,
                    null))
            products.add(ProductVo(9527,
                    "展会7",
                    1440.0,
                    2160.0,
                    2,
                    "模板拼图",
                    1,
                    "1400 x 2160 mm",
                    1,
                    "",
                    100,
                    null))
            products.add(ProductVo(9528,
                    "展会8",
                    1440.0,
                    2160.0,
                    2,
                    "模板拼图",
                    1,
                    "1400 x 2160 mm",
                    1,
                    "",
                    100,
                    null))
            products.add(ProductVo(9529,
                    "展会9",
                    1440.0,
                    2160.0,
                    2,
                    "模板拼图",
                    1,
                    "1400 x 2160 mm",
                    1,
                    "",
                    100,
                    null))
            return RestResponse.ok(products)
        }
        val templateIds = templateDao.findByType(type).map { it.id }
        val products = productDao.findByTemplateIdInAndEnabledOrderBySequence(templateIds, true)
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
                    .map { "/assets/product/images/${it.id}.${it.fileType}" }
                    .firstOrNull()
            ProductVo(it.id,
                    it.name,
                    getPixels(it.template.width),
                    getPixels(it.template.height),
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
                9526 -> return RestResponse.ok(Gson().fromJson(json_9526.inputStream.bufferedReader().use { it.readText() }, TemplateVo::class.java))
                9527 -> return RestResponse.ok(Gson().fromJson(json_9527.inputStream.bufferedReader().use { it.readText() }, TemplateVo::class.java))
                9528 -> return RestResponse.ok(Gson().fromJson(json_9528.inputStream.bufferedReader().use { it.readText() }, TemplateVo::class.java))
                9529 -> return RestResponse.ok(Gson().fromJson(json_9529.inputStream.bufferedReader().use { it.readText() }, TemplateVo::class.java))
            }
        }
        val product = productDao.findOne(id)
        val temp = product.template
        val layerBg = Layer(1, "background", images = mutableListOf())
        if (temp.type == com.unicolour.joyspace.model.ProductType.ID_PHOTO.value) {
            layerBg.images!!.add(Img(1, "sticker", 0.0, 0.0, getPixels(temp.width), getPixels(temp.height), 0.0, "", "${baseUrl}/assets/template/preview/${temp.id}_v${temp.currentVersion}/mask.png"))
        }
        val layerUser = Layer(2, "image", images = mutableListOf())
        val templateImages = templateImageInfoDao.findByTemplateIdAndTemplateVersion(temp.id, temp.currentVersion)
        layerUser.images!!.addAll(templateImages.map { Img(it.id, "user", getPixels(it.x), getPixels(it.y), getPixels(it.width), getPixels(it.height), 0.0, "", "") })
        val scene = Scene(1, "", "page", getPixels(temp.width), getPixels(temp.height), layers = mutableListOf())
        scene.layers!!.add(layerBg)
        scene.layers!!.add(layerUser)
        val templateVo = TemplateVo(temp.id, temp.currentVersion, temp.name, temp.type, listOf())
        templateVo.scenes = listOf(scene)
        return RestResponse.ok(templateVo)
    }

    /**
     * 毫米->像素   360DPI
     */
    private fun getPixels(mm: Double): Double {
        return BigDecimal(mm).multiply(BigDecimal(14.1732288)).setScale(0,BigDecimal.ROUND_HALF_UP).toDouble()
    }

}

fun main(args: Array<String>) {
    println(BigDecimal(152.4).multiply(BigDecimal(14.1732288)).setScale(0,BigDecimal.ROUND_HALF_UP).toDouble())
}
package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.ProductImageFileDao
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ProductService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import javax.transaction.Transactional

@Service
open class ProductServiceImpl : ProductService {
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var productImgFileDao: ProductImageFileDao

    override fun getProductsOfPrintStation(printStationId: Int): List<PrintStationProduct> {
        return printStationProductDao.findByPrintStationId(printStationId)
    }

    @Transactional
    override fun updateProduct(id: Int, name: String, remark: String,
                               width: Double, height: Double, defPrice: Double, minImgCount: Int): Boolean {
        val product = productDao.findOne(id)
        if (product != null) {
            product.name = name
            product.width = width
            product.height = height
            product.defaultPrice = (defPrice * 100).toInt()
            product.minImageCount = minImgCount
            product.enabled = true
            product.remark = remark

            productDao.save(product)

            return true
        }
        else {
            return false
        }
    }

    @Transactional
    override fun createProduct(name: String, remark: String,
                               width: Double, height: Double, defPrice: Double, minImgCount: Int): Product? {
        val product = Product()

        product.name = name
        product.width = width
        product.height = height
        product.defaultPrice = (defPrice * 100).toInt()
        product.minImageCount = minImgCount
        product.enabled = true
        product.remark = remark

        productDao.save(product)

        return product
    }

    @Transactional
    override fun uploadProductImageFile(id: Int, type: ProductImageFileType, imageFile: MultipartFile?): ProductImageFile? {
        val product = productDao.findOne(id)
        if (product != null) {
            if (imageFile != null) {
                val uuid = UUID.randomUUID().toString()
                val file = File(assetsDir, "/product/images/$uuid")
                file.parentFile.mkdirs()

                imageFile.transferTo(file)

                val pb = ProcessBuilder(
                        "magick.exe",
                        "identify",
                        file.absolutePath)

                val process = pb.start()

                var retStr:String = "";
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    retStr = reader.readText()
                }

                val retCode = process.waitFor()

                if (retCode != 0) {
                    file.delete()
                    throw IOException("not value image file")
                }
                else {
                    val patternStr = Pattern.quote(file.absolutePath) + "\\s(\\w+)\\s.*"
                    val pattern = Pattern.compile(patternStr)
                    val matcher = pattern.matcher(retStr)

                    matcher.find()

                    var imgType = matcher.group(1).toLowerCase()
                    if (imgType == "jpeg") {
                        imgType = "jpg"
                    }

                    val productImgFile = ProductImageFile()
                    productImgFile.product = product
                    productImgFile.type = type.value
                    productImgFile.fileType = imgType
                    productImgFileDao.save(productImgFile)

                    val fileWithExt = File(assetsDir, "/product/images/${productImgFile.id}.$imgType")
                    file.renameTo(fileWithExt)

                    return productImgFile
                }
            }
        }

        return null
    }

    override fun deleteProductImageFile(imgFileId: Int): Boolean {
        val productImgFile = productImgFileDao.findOne(imgFileId)
        if (productImgFile != null) {
            productImgFileDao.delete(productImgFile)
            val fileWithExt = File(assetsDir, "/product/images/${productImgFile.id}.${productImgFile.fileType}")
            fileWithExt.delete()

            return true
        }
        else {
            return false
        }
    }

    override fun getDataFetcher(fieldName: String): DataFetcher<Any> {
        return DataFetcher<Any> { environment ->
            val product = environment.getSource<Product>()
            when (fieldName) {
                "type" -> {
                    val type = ProductType.values().find{ it.value == product.type }
                    if (type == null) null else type.name
                }
                else -> null
            }
        }
    }
}


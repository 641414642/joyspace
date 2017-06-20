package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PrintStationProductDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.service.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import javax.transaction.Transactional

@Service
open class ProductServiceImpl : ProductService {
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var printStationProductDao: PrintStationProductDao

    @Autowired
    lateinit var productDao: ProductDao

    override fun getProductsOfPrintStation(printStationId: Int): List<PrintStationProduct> {
        return printStationProductDao.findByPrintStationId(printStationId)
    }

    @Transactional
    override fun updateProduct(id: Int, name: String, sn: String, remark: String,
                               width: Double, height: Double, defPrice: Double, minImgCount: Int): Boolean {
        val product = productDao.findOne(id)
        if (product != null) {
            product.name = name
            product.sn = sn
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
    override fun createProduct(name: String, sn: String, remark: String,
                               width: Double, height: Double, defPrice: Double, minImgCount: Int): Product? {
        val product = Product()

        product.name = name
        product.sn = sn
        product.width = width
        product.height = height
        product.defaultPrice = (defPrice * 100).toInt()
        product.minImageCount = minImgCount
        product.enabled = true
        product.remark = remark

        productDao.save(product)

        return product
    }

    override fun uploadProductImageFiles(id: Int, thumbImgFile: MultipartFile?, previewImgFile: MultipartFile?): Boolean {
        val product = productDao.findOne(id)
        if (product != null) {
            if (thumbImgFile != null) {
                val file = File(assetsDir, "/product/thumb/${product.sn}.jpg")
                file.parentFile.mkdirs()

                thumbImgFile.transferTo(file)
            }

            if (previewImgFile != null) {
                val file = File(assetsDir, "/product/preview/${product.sn}.jpg")
                file.parentFile.mkdirs()

                previewImgFile.transferTo(file)
            }

            return true
        }

        return false
    }
}


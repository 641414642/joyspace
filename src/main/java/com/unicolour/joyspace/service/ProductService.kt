package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.model.Product
import org.springframework.web.multipart.MultipartFile

interface ProductService {
    fun getProductsOfPrintStation(printStationId: Int) : List<PrintStationProduct>

    fun createProduct(name: String, sn: String, remark: String,
                      width: Double, height: Double, defPrice: Double, minImgCount: Int) : Product?

    fun updateProduct(id: Int, name: String, sn: String, remark:String,
                      width: Double, height: Double, defPrice: Double, minImgCount: Int): Boolean

    fun uploadProductImageFiles(id: Int, thumbImgFile: MultipartFile?, previewImgFile: MultipartFile?): Boolean
}
package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.model.ProductImageFileType
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface ProductService {

    fun getProductsOfPrintStation(printStationId: Int) : List<PrintStationProduct>

    fun createProduct(name: String, remark: String,
                      width: Double, height: Double, defPrice: Double, minImgCount: Int) : Product?

    fun updateProduct(id: Int, name: String, remark:String,
                      width: Double, height: Double, defPrice: Double, minImgCount: Int): Boolean

    fun getDataFetcher(fieldName:String): DataFetcher<Any>

    fun uploadProductImageFile(id: Int, type: ProductImageFileType, imageFile: MultipartFile?): Boolean
    fun deleteProductImageFile(imgFileId: Int): Boolean
}
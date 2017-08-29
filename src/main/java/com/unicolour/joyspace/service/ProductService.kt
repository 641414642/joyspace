package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.*
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface ProductService {

    fun getProductsOfPrintStation(printStationId: Int) : List<PrintStationProduct>

    fun createProduct(name: String, remark: String, defPrice: Double, templateId: Int) : Product?

    fun updateProduct(id: Int, name: String, remark:String, defPrice: Double, templateId: Int): Boolean

    fun getDataFetcher(fieldName:String): DataFetcher<Any>

    fun uploadProductImageFile(id: Int, type: ProductImageFileType, imageFile: MultipartFile?): ProductImageFile?
    fun deleteProductImageFile(imgFileId: Int): Boolean
}
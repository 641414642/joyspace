package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.PrintStationProduct
import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.model.ProductImageFile
import com.unicolour.joyspace.model.ProductImageFileType
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface ProductService {

    fun getProductsOfPrintStation(printStationId: Int) : List<PrintStationProduct>

    fun createProduct(name: String, remark: String, defPrice: Double, templateName: String) : Product?

    fun updateProduct(id: Int, name: String, remark:String, defPrice: Double, templateName: String): Boolean

    fun getDataFetcher(fieldName:String): DataFetcher<Any>

    fun uploadProductImageFile(id: Int, type: ProductImageFileType, imageFile: MultipartFile?): ProductImageFile?
    fun deleteProductImageFile(imgFileId: Int): Boolean
}
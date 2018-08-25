package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.*
import graphql.schema.DataFetcher
import org.springframework.data.domain.Page
import org.springframework.web.multipart.MultipartFile

interface ProductService {
    fun getProductsOfPrintStation(printStationId: Int) : List<PrintStationProduct>

    fun getProductsOfPrintStationAndCommonProduct(printStationId: Int) : List<Product>

    fun createProduct(name: String, remark: String, defPrice: Double, areaPrice: Double, piecePrice: Double, templateId: Int, refined: Int) : Product?

    fun updateProduct(id: Int, name: String, remark: String, defPrice: Double, areaPrice: Double, piecePrice: Double, templateId: Int, refined: Int): Boolean

    fun getDataFetcher(fieldName:String): DataFetcher<Any>

    fun uploadProductImageFile(id: Int, type: ProductImageFileType, imageFile: MultipartFile?): ProductImageFile?
    fun deleteProductImageFile(imgFileId: Int): Boolean
    fun moveProduct(id: Int, up: Boolean): Boolean

    fun queryProducts(pageNo: Int, pageSize: Int, companyId: Int, name: String, excludeDeleted: Boolean, order: String): Page<Product>
    fun queryProducts(companyId: Int, name: String, excludeDeleted: Boolean, order: String): List<Product>

    fun deleteProductById(productId: Int): Boolean
}
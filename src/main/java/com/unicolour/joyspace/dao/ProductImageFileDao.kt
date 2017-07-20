package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.ProductImageFile
import org.springframework.data.repository.CrudRepository

interface ProductImageFileDao : CrudRepository<ProductImageFile, Int> {
    fun findByProductIdAndType(productId: Int, type: Int) : List<ProductImageFile>
}

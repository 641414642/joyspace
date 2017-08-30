package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.ProductType

class ProductDTO(
        var id: Int = 0,
        var name: String = "",
        var type: Int = ProductType.PHOTO.value,
        var width: Double = 0.0,
        var height: Double = 0.0,
        var imageRequired: Int = 0,
        var remark: String? = null,
        var price: Int = 0,
        var version: Int = 0,
        var thumbnailUrl: String? = null,
        var previewUrl: String? = null,
        var thumbnailUrls: List<String> = emptyList(), var previewUrls: List<String> = emptyList()
)

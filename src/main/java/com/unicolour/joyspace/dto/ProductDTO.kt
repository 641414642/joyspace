package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.model.ProductType

class ProductDTO(
        var id: Int = 0,
        var name: String = "",
        var type: Int = ProductType.PHOTO.value,
        var sn: String = "",
        var resolutionX: Int = 0,
        var resolutionY: Int = 0,
        var imageRequired: Int = 0,
        var remark: String? = null,
        var price: Int = 0,
        var thumbnailUrl: String = "",
        var previewUrl: String = ""
)

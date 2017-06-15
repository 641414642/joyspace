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
        var thumbnailUrl: String = ""
)

fun Product.productToDTO(baseUrl: String, priceMap: Map<Int, Int>) : ProductDTO =
    ProductDTO(
            id = this.id,
            name = this.name,
            type = this.type,
            sn = this.sn,
            resolutionX = this.resolutionX,
            resolutionY = this.resolutionY,
            imageRequired = this.minImageCount,
            remark = this.remark,
            price = priceMap.getOrDefault(this.id, this.defaultPrice),
            thumbnailUrl = "${baseUrl}/assets/product/thumb/${this.sn}.jpg"
    )


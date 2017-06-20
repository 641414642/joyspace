package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.*

fun User.userToDTO(): UserDTO =
        UserDTO(
            email = this.email,
            userName = this.userName,
            wxOpenId = this.wxOpenId,
            fullName = this.fullName,
            sex = when(this.sex) {
                USER_SEX_MALE -> "M"
                USER_SEX_FEMALE -> "F"
                else -> "N/A"
            },
            phone = this.phone,
            enabled = this.enabled
        )

fun PrintStation.printStationToDTO(): PrintStationDTO {
    val ps = PrintStationDTO()

    ps.sn = this.sn
    ps.address = this.position.address
    ps.wxQrCode = this.wxQrCode
    ps.latitude = this.position.latitude
    ps.longitude = this.position.longitude

    return ps
}

fun PrintStation.printStationToDetailDTO(productsOfPrintStation: List<ProductDTO>): PrintStationDetailDTO {
    val ps = PrintStationDetailDTO()

    ps.sn = this.sn
    ps.address = this.position.address
    ps.wxQrCode = this.wxQrCode
    ps.latitude = this.position.latitude
    ps.longitude = this.position.longitude
    ps.products.addAll(productsOfPrintStation)

    return ps
}

fun Product.productToDTO(baseUrl: String, priceMap: Map<Int, Int>) : ProductDTO =
        ProductDTO(
                id = this.id,
                name = this.name,
                type = this.type,
                sn = this.sn,
                width = this.width,
                height = this.height,
                imageRequired = this.minImageCount,
                remark = this.remark,
                price = priceMap.getOrDefault(this.id, this.defaultPrice),
                thumbnailUrl = "${baseUrl}/assets/product/thumb/${this.sn}.jpg",
                previewUrl = "${baseUrl}/assets/product/preview/${this.sn}.jpg"
        )


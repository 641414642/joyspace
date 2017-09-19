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

    ps.id = this.id
    ps.address = this.position.address
    ps.wxQrCode = this.wxQrCode
    ps.latitude = this.position.latitude
    ps.longitude = this.position.longitude

    return ps
}

fun PrintStation.printStationToDetailDTO(productsOfPrintStation: List<ProductDTO>): PrintStationDetailDTO {
    val ps = PrintStationDetailDTO()

    ps.id = this.id
    ps.address = this.position.address
    ps.wxQrCode = this.wxQrCode
    ps.latitude = this.position.latitude
    ps.longitude = this.position.longitude
    ps.products.addAll(productsOfPrintStation)

    return ps
}

fun Product.productToDTO(baseUrl: String, priceMap: Map<Int, Int>) : ProductDTO {
    val thumbUrls = this.imageFiles.filter { it.type == ProductImageFileType.THUMB.value }.map { "${baseUrl}/assets/product/images/${it.id}.${it.fileType}" }
    val previewUrls = this.imageFiles.filter { it.type == ProductImageFileType.PREVIEW.value }.map { "${baseUrl}/assets/product/images/${it.id}.${it.fileType}" }
    val tpl = this.template

    return ProductDTO(
            id = this.id,
            name = this.name,
            type = tpl.type,
            width = tpl.width,
            height = tpl.height,
            imageRequired = tpl.minImageCount,
            remark = this.remark,
            version = this.version,
            price = priceMap.getOrDefault(this.id, this.defaultPrice),
            thumbnailUrl = thumbUrls.firstOrNull(),
            previewUrl = previewUrls.firstOrNull(),
            thumbnailUrls = thumbUrls,
            previewUrls = previewUrls
    )
}
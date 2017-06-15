package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.PrintStation

open class PrintStationDTO {
    /** 编号 */
    var sn: String = ""
    /** 地址 */
    var address: String = ""
    /** 微信二维码 */
    var wxQrCode: String = ""
    /** 经度 */
    var longitude: Double = 0.0
    /** 纬度 */
    var latitude: Double = 0.0
}

class PrintStationDetailDTO : PrintStationDTO() {
    var products: MutableList<ProductDTO> = ArrayList()
}

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

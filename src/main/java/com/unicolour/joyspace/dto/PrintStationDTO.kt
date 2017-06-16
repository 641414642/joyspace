package com.unicolour.joyspace.dto

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


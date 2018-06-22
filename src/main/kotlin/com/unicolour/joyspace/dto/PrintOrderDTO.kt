package com.unicolour.joyspace.dto

class PrintOrderDTO(
        var id: Int = 0,
        var wxUserNickName: String? = null,
        var printOrderItems: List<PrintOrderItemDTO> = emptyList()
)

class PrintOrderItemDTO(
        var id: Int = 0,
        var copies: Int = 1,
        var productId: Int = 0,
        var productType: Int = 0,
        var productVersion: String = "",
        var refined: Int = 0,
        //用户图片
        var orderImages: List<PrintOrderImageDTO> = emptyList()
)

class PrintOrderImageDTO(
        var id: Int = 0,

        //图片在模板中的名称
        var name: String = "",

        //用户图片
        var userImageFile: UserImageFileDTO? = null,

        //图片处理参数
        var processParams: String? = null
)

class UserImageFileDTO(
        var type: String = "",
        var width: Int = 0,
        var height: Int = 0,
        var url: String = "",
        var fileName: String = ""
)


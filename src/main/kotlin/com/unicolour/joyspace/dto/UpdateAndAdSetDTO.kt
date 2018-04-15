package com.unicolour.joyspace.dto

class UpdateAndAdSetDTO(
        var version: Int = 0,
        var adSet: AdSetDTO? = null
)

class AdSetDTO(
        var id: Int = 0,
        var name: String = "",
        var updateTime: String = "",
        var imageFiles: List<AdSetImageFileDTO> = emptyList()
)

class AdSetImageFileDTO(
        var id: Int = 0,
        var fileName: String = "",
        var fileType: String = "",
        var width: Int = 0,
        var height: Int = 0,
        var description: String = "",
        var duration: Int = 0,
        var sequence: Int = 0,
        var url: String = ""
)
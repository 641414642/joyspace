package com.unicolour.joyspace.dto

import com.fasterxml.jackson.annotation.JsonInclude

class UpdateAndAdSetDTO(
        var version: Int = 0,
        var adSet: AdSetDTO? = null,

        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        var defaultIccFileName: String? = null,

        @get:JsonInclude(JsonInclude.Include.NON_NULL)
        var iccConfigs: List<IccConfigDTO>? = null
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

class IccConfigDTO(
        var printerModel: String = "",
        var osName: String? = null,
        var iccFileName: String = ""
)
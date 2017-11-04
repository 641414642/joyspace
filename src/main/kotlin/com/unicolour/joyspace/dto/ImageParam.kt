package com.unicolour.joyspace.dto

open class ImageProcessParams(
    var initialRotate: Int = 0,
    var scale: Double = 1.0,
    var rotate: Double = 0.0,
    var horTranslate: Double = 0.0,
    var verTranslate: Double = 0.0,
    var brightness: Double = 1.0,
    var saturate: Double = 1.0,
    var effect: String = "none") {

    constructor(param: ImageProcessParams) : this(
            param.initialRotate,
            param.scale,
            param.rotate,
            param.horTranslate,
            param.verTranslate,
            param.brightness,
            param.saturate,
            param.effect
    )
}

class ImageParam(
        var name: String = "",
        var imageId: Int = 0
) : ImageProcessParams()

class PreviewParam(
        var sessionId: String = "",
        var productId: Int = 0,
        var productVersion: String = "",
        var images: List<ImageParam> = emptyList()
)

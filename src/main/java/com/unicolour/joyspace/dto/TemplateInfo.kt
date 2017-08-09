package com.unicolour.joyspace.dto

class TemplateInfo(
        val widthInMM: Double,
        val heightInMM: Double,
        val minImageCount: Int,
        val images: List<TemplateImageInfo> = emptyList()
)


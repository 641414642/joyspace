package com.unicolour.joyspace.dto

class ProductTypeItem(
        val productTypeId: Int,
        val productTypeName: String,
        val selected: Boolean
)

class ProductItem(
        val productId: Int,
        val productType: Int,
        val productName: String,
        val templateName: String,
        val selected: Boolean
)

class PositionItem(
        val positionId: Int,
        val positionName: String,
        val address: String,
        val selected: Boolean
)

class PrintStationItem(
        val printStationId: Int,
        val printStationName: String,
        val address: String,
        val selected: Boolean
)


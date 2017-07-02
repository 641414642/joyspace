package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.Position
import graphql.schema.DataFetcher

interface PositionService {
    fun createPosition(name: String, address: String, longitude: Double, latitude: Double, priceListId: Int) : Position?
    fun updatePosition(id: Int, name: String, address: String, longitude: Double, latitude: Double, priceListId: Int): Boolean
}
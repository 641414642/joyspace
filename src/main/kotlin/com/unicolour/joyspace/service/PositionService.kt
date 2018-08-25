package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.AddressComponent
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.model.PositionImageFile
import org.springframework.web.multipart.MultipartFile

interface PositionService {
    fun createPosition(name: String, shortName: String, address: String, transportation: String, longitude: Double, latitude: Double, priceListId: Int) : Position?
    fun updatePosition(id: Int, name: String, shortName: String, address: String, transportation: String, longitude: Double, latitude: Double, priceListId: Int): Boolean
    fun uploadPositionImageFile(id: Int, imageFile: MultipartFile?): PositionImageFile?
    fun deletePositionImageFile(imgFileId: Int): Boolean
    fun getAddressComponent(longitude: Double, latitude: Double): AddressComponent?
}
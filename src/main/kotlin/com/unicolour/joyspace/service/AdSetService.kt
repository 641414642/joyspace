package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.AdSetDTO
import com.unicolour.joyspace.dto.AdSetImageDTO
import com.unicolour.joyspace.model.AdImageFile
import com.unicolour.joyspace.model.AdSet
import org.springframework.web.multipart.MultipartFile

interface AdSetService {
    fun createAdSet(name: String, publicResource: Boolean, imgFiles: List<AdSetImageDTO>)
    fun updateAdSet(id: Int, name: String, publicResource: Boolean, imgFiles: List<AdSetImageDTO>): Boolean
    fun getAdImageUrl(adImageFile: AdImageFile): String
    fun getAdThumbImageUrl(adImageFile: AdImageFile): String
    fun adSetToDTO(adSet: AdSet?): AdSetDTO?
    fun uploadAdSetImageFile(imageFile: MultipartFile?): Array<String>?
}

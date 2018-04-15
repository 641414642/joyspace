package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.AdSetDTO
import com.unicolour.joyspace.model.AdImageFile
import com.unicolour.joyspace.model.AdSet
import javax.servlet.http.Part

interface AdSetService {
    fun createAdSet(name: String, publicResource: Boolean, imgFiles: List<Pair<Part, Int>>)
    fun updateAdSet(id: Int, name: String, publicResource: Boolean, imgFiles: List<Pair<Part, Int>>, adSetIdDurationMap: HashMap<Int, Int>): Boolean
    fun getAdImageUrl(adImageFile: AdImageFile): String
    fun adSetToDTO(adSet: AdSet?): AdSetDTO?
}

package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.AdImageFile
import javax.servlet.http.Part

interface AdSetService {
    fun createAdSet(name: String, publicResource: Boolean, imgFiles: List<Pair<Part, Int>>)
    fun updateAdSet(id: Int, name: String, publicResource: Boolean, imgFiles: List<Pair<Part, Int>>, adSetIdDurationMap: HashMap<Int, Int>): Boolean
    fun getAdImageUrl(adImageFile: AdImageFile): String
}

package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.AdImageFile
import javax.servlet.http.Part

interface AdSetService {
    fun createAdSet(name: String, imgFiles: List<Pair<Part, Int>>)
    fun updateAdSet(id: Int, name: String, imgFiles: List<Pair<Part, Int>>): Boolean
    fun getAdImageUrl(baseUrl: String, adImageFile: AdImageFile): String
}

package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.AdImageFile

interface AdSetService {
    fun createAdSet(name: String)
    fun updateAdSet(id: Int, name: String): Boolean
    fun getAdImageUrl(baseUrl: String, adImageFile: AdImageFile): String
}

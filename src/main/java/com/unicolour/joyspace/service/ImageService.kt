package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun uploadImage(sessionId: String, thumbMaxWidth: Int, thumbMaxHeight: Int, imgFile: MultipartFile?, baseUrl: String): ImageInfo
    fun deleteImage(sessionId: String, imageId: Int): CommonRequestResult
    fun getImageFileUrlDataFetcher(): DataFetcher<String>
}
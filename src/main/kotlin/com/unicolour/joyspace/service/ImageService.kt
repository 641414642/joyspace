package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun uploadImage(sessionId: String, imgFile: MultipartFile?, baseUrl: String): ImageInfo
    fun createThumbnail(sessionId: String, userImgFile: UserImageFile, width: Int, height: Int): UserImageFile?

    fun deleteImage(sessionId: String, imageId: Int): CommonRequestResult
    fun getImageUrl(baseUrl:String, userImgFile: UserImageFile) : String

    fun getImageFileUrl(userImgFile: UserImageFile): String
    fun getImageFileUrlDataFetcher(): DataFetcher<String>
}

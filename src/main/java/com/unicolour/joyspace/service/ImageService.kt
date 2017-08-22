package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun uploadImage(sessionId: String, imgFile: MultipartFile?, baseUrl: String): ImageInfo
    fun resizeImage(sessionId: String, imageId: Int, width: Int, height: Int, baseUrl: String): ImageInfo
    fun rotateAndCropImage(sessionId: String, imageId: Int, angleDeg: Double, cropX: Double, cropY: Double, cropWid: Double, cropHei: Double, baseUrl: String): ImageInfo

    fun deleteImage(sessionId: String, imageId: Int): CommonRequestResult

    fun getImageUrl(baseUrl:String, userImgFile: UserImageFile) : String
    fun getImageFileUrlDataFetcher(): DataFetcher<String>
}

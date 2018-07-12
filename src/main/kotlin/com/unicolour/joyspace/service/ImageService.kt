package com.unicolour.joyspace.service

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageFileDimensionAndType
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.UserImageFile
import graphql.schema.DataFetcher
import org.springframework.web.multipart.MultipartFile
import java.io.File

interface ImageService {
    fun uploadImage(sessionId: String, imgFile: MultipartFile?): ImageInfo
    fun createThumbnail(sessionId: String, userImgFile: UserImageFile, width: Int, height: Int): UserImageFile?

    fun deleteImage(sessionId: String, imageId: Int): CommonRequestResult
    fun getImageUrl(userImgFile: UserImageFile) : String

    fun getImageFileUrl(userImgFile: UserImageFile): String
    fun getImageFileUrlDataFetcher(): DataFetcher<String>
    fun getImageFileDimensionAndType(imageFile: File): ImageFileDimensionAndType
    fun createThumbnailImageFile(srcImgFile: File, geometry: String, thumbImgFile: File)   //geometry   http://www.imagemagick.org/script/command-line-processing.php#geometry
}

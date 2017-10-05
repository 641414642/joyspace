package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.service.ImageService
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest


@RestController
class ApiImageController {
    @Autowired
    lateinit var imageService: ImageService

    @RequestMapping("/api/image", method = arrayOf(RequestMethod.POST))
    fun uploadImage(request: HttpServletRequest,
                    @RequestParam("sessionId") sessionId: String,
                    @RequestParam("image") imgFile: MultipartFile?) : ResponseEntity<ImageInfo> {
        val baseUrl = getBaseUrl(request)
        val imgInfo = imageService.uploadImage(sessionId, imgFile, baseUrl)

        return ResponseEntity.ok(imgInfo)
    }

    @RequestMapping("/api/image/resize", method = arrayOf(RequestMethod.POST))
    fun resizeImage(request: HttpServletRequest,
                    @RequestParam("sessionId") sessionId: String,
                    @RequestParam("width") width: Int,
                    @RequestParam("height") height: Int,
                    @RequestParam("imageId") imageId: Int) : ResponseEntity<ImageInfo> {
        val baseUrl = getBaseUrl(request)
        val imgInfo = imageService.resizeImage(sessionId, imageId, width, height, baseUrl)

        return ResponseEntity.ok(imgInfo)
    }

    @RequestMapping("/api/image/rotateAndCrop", method = arrayOf(RequestMethod.POST))
    fun rotateAndCropImage(request: HttpServletRequest,
                           @RequestParam("sessionId") sessionId: String,
                           @RequestParam("imageId") imageId: Int,
                           @RequestParam("angleDeg") angleDeg: Double,
                           @RequestParam("cropX") cropX: Double,
                           @RequestParam("cropY") cropY: Double,
                           @RequestParam("cropWid") cropWid: Double,
                           @RequestParam("cropHei") cropHei: Double
    ): ResponseEntity<ImageInfo> {
        val baseUrl = getBaseUrl(request)
        val imgInfo = imageService.rotateAndCropImage(sessionId, imageId, angleDeg, cropX, cropY, cropWid, cropHei, baseUrl)

        return ResponseEntity.ok(imgInfo)
    }
    @RequestMapping(path = arrayOf("/api/image/transform"), method = arrayOf(RequestMethod.POST))
    fun transformImage(request: HttpServletRequest,
                           @RequestParam("sessionId") sessionId: String,
                           @RequestParam("imageId") imageId: Int,
                           @RequestParam("imageRatio") imageRatio: Double,
                           @RequestParam("initialRotate") initialRotate: Int,
                           @RequestParam("translateX") translateX: Int,
                           @RequestParam("translateY") translateY: Int,
                           @RequestParam("scale") scale: Double,
                           @RequestParam("rotate") rotate: Double
    ): ResponseEntity<ImageInfo> {
        val baseUrl = getBaseUrl(request)
        val imgInfo = imageService.transformImage(sessionId, imageId, imageRatio, initialRotate, translateX, translateY, scale, rotate, baseUrl)

        return ResponseEntity.ok(imgInfo)
    }

    @RequestMapping("/api/image", method = arrayOf(RequestMethod.DELETE))
    fun deleteImage(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("imageId") imageId: Int) : ResponseEntity<CommonRequestResult> {
        val result = imageService.deleteImage(sessionId, imageId)
        return ResponseEntity.ok(result)
    }
}


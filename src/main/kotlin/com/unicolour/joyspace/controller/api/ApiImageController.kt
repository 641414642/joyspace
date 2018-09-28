package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.service.ImageService
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
    fun uploadImage(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("image") imgFile: MultipartFile?,
                    @RequestParam("filterImageId",required = false)filterImageId:String?) : ResponseEntity<ImageInfo> {
        val imgInfo = imageService.uploadImage(filterImageId,sessionId, imgFile)

        return ResponseEntity.ok(imgInfo)
    }

    @RequestMapping("/api/image", method = arrayOf(RequestMethod.DELETE))
    fun deleteImage(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("imageId") imageId: Int) : ResponseEntity<CommonRequestResult> {
        val result = imageService.deleteImage(sessionId, imageId)
        return ResponseEntity.ok(result)
    }
}


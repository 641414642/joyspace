package com.unicolour.joyspace.controller.app

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
class AppImageController {
    @Autowired
    lateinit var imageService: ImageService

    @RequestMapping("/app/image", method = arrayOf(RequestMethod.POST))
    fun uploadImage(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("imageFile") imgFiles: Array<MultipartFile>,
                    @RequestParam("imageFileMD5") imgFileMD5s : Array<String>,
                    @RequestParam("filterImageId",required = false) filterImageId:String?) : ResponseEntity<List<ImageInfo>> {
        val imgInfoList = ArrayList<ImageInfo>()
        var i = 0;
        for (imgFile in imgFiles) {
            val md5 = imgFileMD5s[i++]
            val imgInfo = imageService.uploadImage(filterImageId,sessionId, imgFile)
            imgInfoList.add(imgInfo)
            imgInfo.errmsg = md5
        }

        return ResponseEntity.ok(imgInfoList)
    }

    @RequestMapping("app/image", method = arrayOf(RequestMethod.DELETE))
    fun deleteImage(@RequestParam("sessionId") sessionId: String,
                    @RequestParam("imageId") imageId: Int) : ResponseEntity<CommonRequestResult> {
        val result = imageService.deleteImage(sessionId, imageId)
        return ResponseEntity.ok(result)
    }
}
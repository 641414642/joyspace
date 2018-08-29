package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@RestController
class ApiImageRoute {
    companion object {
        val logger = LoggerFactory.getLogger(ApiImageRoute::class.java)
    }

    @Autowired
    lateinit var imageService: ImageService


    /**
     * 滤镜列表接口
     */
    @GetMapping(value = "/v2/filter/filterList")
    fun filterList(@RequestParam("sessionId") sessionId: String): RestResponse? {
        return try {
            val imgInfo = imageService.filterImageList(sessionId)
            logger.info("filterList:$imgInfo")
            RestResponse.ok(imgInfo)
        } catch (e: ProcessException) {
            logger.error("error code:${e.errcode},message:${e.message}", e)
            RestResponse(e.errcode, null, e.message)
        } catch (e: Exception) {
            logger.error("error occurs while filterList", e)
            RestResponse(1, null, e.message)
        }
    }


    /**
     * 根据前段传来的图片生成效果图
     */
    @PostMapping(value = "/v2/fileter/fileterImage")
    fun fileterImage(@RequestParam("sessionId") sessionId: String?,
                     @RequestParam("imgFile") imgFile: MultipartFile?): String?{
        return imageService.imageToFilter(sessionId,imgFile)
    }



    @PostMapping("v2/image/convert")
    fun convert() {
        try {
            val srcImage = "/root/joyTest/test0805.jpeg"
            val desImage = "/root/joyTest/test0805_.jpeg"

            val process = ProcessBuilder("python", "/root/JoySpace-Filter/joy_filter.py",srcImage,desImage).start()

            var retStr = ""
            var retError = ""
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                retStr = reader.readText()
            }
            BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                retError = reader.readText()
            }

            val retCode = process.waitFor()
            println("retStr:$retStr,retCode:$retCode,retError:$retError")

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
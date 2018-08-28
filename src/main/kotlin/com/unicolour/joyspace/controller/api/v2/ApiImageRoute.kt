package com.unicolour.joyspace.controller.api.v2

import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.Filter
import com.unicolour.joyspace.dto.FilterListVo
import com.unicolour.joyspace.dto.FilterUrl
import com.unicolour.joyspace.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
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

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao


    /**
     * 滤镜列表接口
     */
    @GetMapping(value = "/v2/filter/filterList")
    fun filterList(@RequestParam("sessionId") sessionId: String): FilterListVo? {
        val imgInfo = imageService.fileterImageList(sessionId)
        logger.info("filterList:${imgInfo}")
        return imgInfo
    }


    /**
     * 根据前段传来的图片生成效果图
     */
    @GetMapping(value = "/v2/fileter/fileterImage")
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
package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PrintOrderProductImageDao
import com.unicolour.joyspace.dao.UserImageFileDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.FilterListVo
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.dto.common.RestResponse
import com.unicolour.joyspace.model.PrintOrderImageStatus
import com.unicolour.joyspace.model.PrintOrderProductImage
import com.unicolour.joyspace.service.ImageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletRequest

@Controller
class FilterController{

    companion object {
        val logger = LoggerFactory.getLogger(FilterController::class.java)
    }

    @Autowired
    lateinit var imageService: ImageService

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao


    /**
     * 滤镜列表接口
     */
    @RequestMapping(value = "/filter/filterList",method = arrayOf(RequestMethod.POST))
    fun filterList(@RequestParam("sessionId") sessionId: String): FilterListVo? {
        val imgInfo = imageService.fileterImageList(sessionId)
        logger.info("filterList:${imgInfo}")
        return imgInfo
    }


    /**
     * 根据前段传来的图片生成效果图
     */
    @RequestMapping(value = "/fileter/fileterImage",method = arrayOf(RequestMethod.POST))
    fun fileterImage(@RequestParam("sessionId") sessionId: String?,
                     @RequestParam("image") imgFile: MultipartFile?): RestResponse{

        var user =  userLoginSessionDao.findOne(sessionId);
        if (user == null) {
            return RestResponse.error(ResultCode.INVALID_USER_LOGIN_SESSION)
        } else {

        }
        return RestResponse.ok()
    }

}
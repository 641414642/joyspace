package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.UserImageFileDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.ImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

@Service
class ImageServiceImpl : ImageService {
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var userLoginSession: UserLoginSessionDao

    override fun uploadImage(sessionId: String, thumbMaxWidth: Int, thumbMaxHeight: Int, imgFile: MultipartFile?, baseUrl: String): ImageInfo {
        val session = userLoginSession.findOne(sessionId);

        if (session == null) {
            return ImageInfo(0,0, 0, "", 0, 0, 1, "用户未登录")
        }
        else if (imgFile == null) {
            return ImageInfo(0,0, 0, "", 0, 0, 2, "没有图片文件")
        }
        else {
            val fileName = UUID.randomUUID().toString().replace("-", "")
            val filePath = "user/${session.userId}/${sessionId}/${fileName}"
            val file = File(assetsDir, filePath)
            val thumbFile = File(assetsDir, "${filePath}.thumb.jpg")
            file.parentFile.mkdirs()

            imgFile.transferTo(file)

            val pb = ProcessBuilder(
                    "magick.exe",
                    file.absolutePath,
                    "-thumbnail",
                    "${thumbMaxWidth}x${thumbMaxHeight}",
                    "-identify",
                    thumbFile.absolutePath)

            val process = pb.start()

            var retStr:String = "";
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                retStr = reader.readText()
            }

            val retCode = process.waitFor()

            if (retCode != 0) {
                return ImageInfo(0,0, 0, "", 0, 0, 3, retStr)
            }
            else {
                val patternStr = Pattern.quote(file.absolutePath) + "\\s(\\w+)\\s(\\d+)x(\\d+)=>(\\d+)x(\\d+).*"
                val pattern = Pattern.compile(patternStr)
                val matcher = pattern.matcher(retStr)

                matcher.find()

                val imgType = matcher.group(1).toLowerCase()
                val imgWid = matcher.group(2).toInt()
                val imgHei = matcher.group(3).toInt()
                val thumbWid = matcher.group(4).toInt()
                val thumbHei = matcher.group(5).toInt()

                val thumbUrl = "${baseUrl}/assets/${filePath}.thumb.jpg"

                val fileWithExt = File(assetsDir, "${filePath}.${imgType}")
                file.renameTo(fileWithExt)

                val userImgFile = UserImageFile()
                userImgFile.type = imgType
                userImgFile.fileName = fileName
                userImgFile.width = imgWid
                userImgFile.height = imgHei
                userImgFile.sessionId = sessionId
                userImgFile.uploadTime = Calendar.getInstance()
                userImgFile.userId = session.userId

                userImageFileDao.save(userImgFile)

                return ImageInfo(userImgFile.id, imgWid, imgHei, thumbUrl, thumbWid, thumbHei)
            }
        }
    }

    override fun deleteImage(sessionId: String, imageId: Int): CommonRequestResult {
        val session = userLoginSession.findOne(sessionId);

        if (session == null) {
            return CommonRequestResult(1, "用户未登录")
        }
        else {
            val imageFile =  userImageFileDao.findOne(imageId);
            if (imageFile == null) {
                return CommonRequestResult(2, "图片不存在")
            }
            else if (imageFile.userId != session.userId) {
                return CommonRequestResult(3, "图片不属于当前登录用户")
            }
            else {
                val filePath = "user/${imageFile.userId}/${imageFile.sessionId}/${imageFile.fileName}"
                val file = File(assetsDir, "${filePath}/.${imageFile.type}")
                val thumbFile = File(assetsDir, "${filePath}.thumb.jpg")

                file.delete()
                thumbFile.delete()

                userImageFileDao.delete(imageFile)

                return CommonRequestResult(0, null)
            }
        }
    }
}
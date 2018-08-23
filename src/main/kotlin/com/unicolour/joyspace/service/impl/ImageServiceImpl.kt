package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageFileDimensionAndType
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.ImageService
import graphql.schema.DataFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class ImageServiceImpl : ImageService {

    companion object {
        val logger = LoggerFactory.getLogger(PositionServiceImpl::class.java)
    }
    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var productDao: ProductDao

    override fun uploadImage(sessionId: String, imgFile: MultipartFile?): ImageInfo {
        val session = userLoginSessionDao.findOne(sessionId);

        if (session == null) {
            return ImageInfo(1, "用户未登录")
        }
        else if (imgFile == null) {
            return ImageInfo(2, "没有图片文件")
        }
        else {
            try {
                val fileName = UUID.randomUUID().toString().replace("-", "")
                val filePath = "user/${session.userId}/${sessionId}/${fileName}"
                val file = File(assetsDir, filePath)
                file.parentFile.mkdirs()

                imgFile.transferTo(file)

                logger.info("file.absolutePath:${file.absolutePath}")

                val pb = ProcessBuilder("magick", "identify", file.absolutePath)

                val process = pb.start()

                var retStr:String = "";
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    retStr = reader.readText()
                }

                val retCode = process.waitFor()

                if (retCode != 0) {
                    logger.error("图片处理失败，retStr:$retStr , retCode: $retCode")
                    return ImageInfo(3, retStr)
                }
                else {
                    val patternStr = Pattern.quote(file.absolutePath) + "\\s(\\w+)\\s(\\d+)x(\\d+)\\s.*"
                    val pattern = Pattern.compile(patternStr)
                    val matcher = pattern.matcher(retStr)

                    matcher.find()

                    var imgType = matcher.group(1).toLowerCase()
                    if (imgType == "jpeg") {
                        imgType = "jpg"
                    }
                    val imgWid = matcher.group(2).toInt()
                    val imgHei = matcher.group(3).toInt()


                    val fileWithExt = File(assetsDir, "${filePath}.${imgType}")
                    file.renameTo(fileWithExt)

                    val url = "${baseUrl}/assets/${filePath}.${imgType}"

                    val userImgFile = UserImageFile()
                    userImgFile.type = imgType
                    userImgFile.fileName = fileName
                    userImgFile.width = imgWid
                    userImgFile.height = imgHei
                    userImgFile.sessionId = sessionId
                    userImgFile.uploadTime = Calendar.getInstance()
                    userImgFile.userId = session.userId

                    userImageFileDao.save(userImgFile)

                    return ImageInfo(0, null, userImgFile.id, imgWid, imgHei, url)
                }
            } catch (e: Exception) {
                logger.error("error occurs while ",e)
                return ImageInfo(3, "")
            }
        }
    }

    override fun createThumbnail(sessionId: String, userImgFile: UserImageFile, width: Int, height: Int) : UserImageFile? {
        val destFileName = UUID.randomUUID().toString().replace("-", "")
        val destFilePath = "user/${userImgFile.userId}/${sessionId}/${destFileName}.jpg"

        val destFile = File(assetsDir, destFilePath)

        val srcFilePath = "user/${userImgFile.userId}/${userImgFile.sessionId}/${userImgFile.fileName}.${userImgFile.type}"
        val srcFile = File(assetsDir, srcFilePath)

        val pb = ProcessBuilder(
                "magick",
                "convert",
                srcFile.absolutePath,
                "-thumbnail",
                "${width}x${height}",
                "-identify",
                destFile.absolutePath)

        val process = pb.start()

        var retStr: String = "";
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            retStr = reader.readText()
        }

        val retCode = process.waitFor()

        if (retCode != 0) {
            return null
        } else {
            val patternStr = Pattern.quote(srcFile.absolutePath) + "\\s\\w+\\s\\d+x\\d+=>(\\d+)x(\\d+).*"
            val pattern = Pattern.compile(patternStr)
            val matcher = pattern.matcher(retStr)

            matcher.find()

            val destImgWid = matcher.group(1).toInt()
            val destImgHei = matcher.group(2).toInt()

            val newUserImgFile = UserImageFile()
            newUserImgFile.type = "jpg"
            newUserImgFile.fileName = destFileName
            newUserImgFile.width = destImgWid
            newUserImgFile.height = destImgHei
            newUserImgFile.sessionId = sessionId
            newUserImgFile.uploadTime = Calendar.getInstance()
            newUserImgFile.userId = userImgFile.userId
            userImageFileDao.save(newUserImgFile)

            userImgFile.thumbnail = newUserImgFile
            userImageFileDao.save(userImgFile)

            return newUserImgFile
        }
    }

    override fun deleteImage(sessionId: String, imageId: Int): CommonRequestResult {
        val session = userLoginSessionDao.findOne(sessionId);

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

    override fun getImageFileUrlDataFetcher(): DataFetcher<String> {
        return DataFetcher { env ->
            val imageFile = env.getSource<UserImageFile>()
            "${baseUrl}/assets/user/${imageFile.userId}/${imageFile.sessionId}/${imageFile.fileName}.${imageFile.type}"
        }
    }

    override fun getImageUrl(userImgFile: UserImageFile): String {
        return "${baseUrl}/assets/user/${userImgFile.userId}/${userImgFile.sessionId}/${userImgFile.fileName}.${userImgFile.type}"
    }

    override fun getImageFileUrl(userImgFile: UserImageFile): String {
        val file = File(assetsDir, "/user/${userImgFile.userId}/${userImgFile.sessionId}/${userImgFile.fileName}.${userImgFile.type}")
        return file.toURI().toURL().toExternalForm()
    }

    override fun getImageFileDimensionAndType(imageFile: File): ImageFileDimensionAndType {
        val pb = ProcessBuilder("magick", "identify", imageFile.absolutePath)

        val process = pb.start()

        var retStr = ""
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            retStr = reader.readText()
        }

        val retCode = process.waitFor()

        if (retCode != 0) {
            imageFile.delete()
            throw IOException("not valid image file")
        } else {
            val patternStr = Pattern.quote(imageFile.absolutePath) + "\\s(\\w+)\\s(\\d+)x(\\d+)\\s.*"
            val pattern = Pattern.compile(patternStr)
            val matcher = pattern.matcher(retStr)

            matcher.find()

            var imgType = matcher.group(1).toLowerCase()
            if (imgType == "jpeg") {
                imgType = "jpg"
            }
            val imgWid = matcher.group(2).toInt()
            val imgHei = matcher.group(3).toInt()

            return ImageFileDimensionAndType(width = imgWid, height = imgHei, type = imgType)
        }
    }

    override fun createThumbnailImageFile(srcImgFile: File, geometry: String, thumbImgFile: File) {
        val pb = ProcessBuilder("magick", "convert", "-resize", geometry, srcImgFile.absolutePath, thumbImgFile.absolutePath)
        val process = pb.start()
        process.waitFor()
    }

    /**
     * 调用python,获取滤镜风格列表
     */
    override fun uploadFileterImage(sessionId: String): String {
        val session = userLoginSessionDao.findOne(sessionId);
        if (session == null) {
            return "用户未登录"
        } else {
            try {
                val fileName = UUID.randomUUID().toString().replace("-", "")
                val filePath = "fileter//${sessionId}/${fileName}"
                val file = File(assetsDir, filePath)
                file.parentFile.mkdirs()

                val srcImage = "/root/fileterImage/fileter${fileName}.jpeg"

                val desImage = "/root/fileterImage/fileter_${fileName}.jpeg"

                logger.info("uploadFileterImage srcImage:${srcImage},desImage:${desImage}")


                var filterImageJson = ProcessBuilder("python", "/root/joy_style/joy_api.py", srcImage, desImage);

                var process = filterImageJson.start();

                var retStr: String = "";
                var retError: String = ""
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    retStr = reader.readText()
                }

                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    retError = reader.readText()
                }
                val retCode = process.waitFor()

                logger.info("uploadFileterImage retStr = ${retStr}, retError = ${retError}, retCode = ${retCode}")

                if (retCode != 0) {
                    logger.error("获取滤镜失败，请稍后重试 ，retStr:$retStr , retCode: $retCode , retError: $retError")
                    return "获取滤镜失败，请稍后重试"
                } else {
                    val patternStr = Pattern.quote(file.absolutePath) + "\\s(\\w+)\\s(\\d+)x(\\d+)\\s.*"
                    val pattern = Pattern.compile(patternStr)
                    val matcher = pattern.matcher(retStr)
                    matcher.find()
                    var imgType = matcher.group(1).toLowerCase()
                    if (imgType == "jpeg") {
                        imgType = "jpg"
                    }


                    val fileWithExt = File(assetsDir, "${filePath}_${fileName}.${imgType}")
                    file.renameTo(fileWithExt)

                    val url = "${baseUrl}/assets/${filePath}.${imgType}"

                    return url
                }
            } catch (e: Exception) {
                logger.error("error occurs while ", e)
                return "获取滤镜失败"
            }
        }
    }
}


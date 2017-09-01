package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.CommonRequestResult
import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.service.ImageService
import graphql.schema.DataFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class ImageServiceImpl : ImageService {
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var productDao: ProductDao

    override fun uploadImage(sessionId: String, imgFile: MultipartFile?, baseUrl: String): ImageInfo {
        val session = userLoginSessionDao.findOne(sessionId);

        if (session == null) {
            return ImageInfo(1, "用户未登录")
        }
        else if (imgFile == null) {
            return ImageInfo(2, "没有图片文件")
        }
        else {
            val fileName = UUID.randomUUID().toString().replace("-", "")
            val filePath = "user/${session.userId}/${sessionId}/${fileName}"
            val file = File(assetsDir, filePath)
            file.parentFile.mkdirs()

            imgFile.transferTo(file)

            val pb = ProcessBuilder("magick", "identify", file.absolutePath)

            val process = pb.start()

            var retStr:String = "";
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                retStr = reader.readText()
            }

            val retCode = process.waitFor()

            if (retCode != 0) {
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
        }
    }

    private fun processImage(sessionId: String, imageId: Int, process : (userImgFile: UserImageFile, srcFile: File) -> ImageInfo) : ImageInfo {
        val session = userLoginSessionDao.findOne(sessionId);

        if (session == null) {
            return ImageInfo(1, "用户未登录")
        } else {
            val userImg = userImageFileDao.findOne(imageId)
            if (userImg == null) {
                return ImageInfo(2, "没有找到指定ID对应的图片")
            } else if (userImg.userId != session.userId) {
                return ImageInfo(3, "不是此用户的图片")
            } else {
                val srcFilePath = "user/${userImg.userId}/${userImg.sessionId}/${userImg.fileName}.${userImg.type}"
                val srcFile = File(assetsDir, srcFilePath)

                if (!srcFile.exists()) {
                    return ImageInfo(4, "图片文件已删除")
                } else {
                    return process(userImg, srcFile)
                }
            }
        }
    }

    override fun resizeImage(sessionId: String, imageId: Int, width: Int, height: Int, baseUrl: String): ImageInfo {
        return processImage(sessionId, imageId,
                { userImgFile, srcFile ->
                    val destFileName = UUID.randomUUID().toString().replace("-", "")
                    val destFilePath = "user/${userImgFile.userId}/${sessionId}/${destFileName}.jpg"

                    val destFile = File(assetsDir, destFilePath)
                    srcFile.parentFile.mkdirs()

                    val pb = ProcessBuilder(
                            "magick",
                            "convert",
                            srcFile.absolutePath,
                            "-thumbnail",
                            "${width}x${height}!",
                            "-identify",
                            destFile.absolutePath)

                    val process = pb.start()

                    var retStr: String = "";
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        retStr = reader.readText()
                    }

                    val retCode = process.waitFor()

                    if (retCode != 0) {
                        ImageInfo(5, retStr)
                    } else {
                        val patternStr = Pattern.quote(srcFile.absolutePath) + "\\s\\w+\\s\\d+x\\d+=>(\\d+)x(\\d+).*"
                        val pattern = Pattern.compile(patternStr)
                        val matcher = pattern.matcher(retStr)

                        matcher.find()

                        val destImgWid = matcher.group(1).toInt()
                        val destImgHei = matcher.group(2).toInt()

                        val destUrl = "${baseUrl}/assets/${destFilePath}"

                        val newUserImgFile = UserImageFile()
                        newUserImgFile.type = "jpg"
                        newUserImgFile.fileName = destFileName
                        newUserImgFile.width = destImgWid
                        newUserImgFile.height = destImgHei
                        newUserImgFile.sessionId = sessionId
                        newUserImgFile.uploadTime = Calendar.getInstance()
                        newUserImgFile.userId = userImgFile.userId

                        userImageFileDao.save(newUserImgFile)

                        ImageInfo(0, null, newUserImgFile.id, destImgWid, destImgHei, destUrl)
                    }
                }
        )
    }

    override fun rotateAndCropImage(sessionId: String, imageId: Int,
                                    angleDeg: Double,
                                    cropX: Double, cropY: Double, cropWid: Double, cropHei: Double,
                                    baseUrl: String): ImageInfo {
        return processImage(sessionId, imageId,
                { userImgFile, srcFile ->
                    if (angleDeg == 0.0 && cropX == 0.0 && cropY == 0.0 && cropWid == 1.0 && cropHei == 1.0) {
                        val destUrl = "${baseUrl}/assets/user/${userImgFile.userId}/${userImgFile.sessionId}/${userImgFile.fileName}.${userImgFile.type}"
                        return@processImage ImageInfo(0, null, userImgFile.id, userImgFile.width, userImgFile.height, destUrl)
                    }

                    val result = rotateAndCropCalculation(userImgFile.width, userImgFile.height, angleDeg)

                    val destFileName = UUID.randomUUID().toString().replace("-", "")
                    val destFilePath = "user/${userImgFile.userId}/${sessionId}/${destFileName}.jpg"

                    val destFile = File(assetsDir, destFilePath)
                    srcFile.parentFile.mkdirs()

                    val commandList = ArrayList<String>()

                    commandList += "magick"
                    commandList += "convert"
                    commandList += srcFile.absolutePath

                    if (angleDeg != 0.0) {
                        commandList += "-rotate"
                        commandList += angleDeg.toString()
                        commandList += "+repage"
                    }

                    val innerRectX = result.innerRect.p1.x
                    val innerRectY = result.innerRect.p1.y
                    val innerRectWid = result.innerRect.width
                    val innerRectHei = result.innerRect.height

                    val cx = (innerRectX + innerRectWid * cropX + 0.5).toInt()
                    val cy = (innerRectY + + innerRectHei * cropY + 0.5).toInt()
                    val cw = (innerRectWid * cropWid + 0.5).toInt()
                    val ch = (innerRectHei * cropHei + 0.5).toInt()

                    commandList += "-crop"
                    commandList += "${cw}x${ch}+${cx}+${cy}"
                    commandList += "+repage"

                    commandList += "-identify"
                    commandList += destFile.absolutePath

                    val pb = ProcessBuilder(commandList)

                    val process = pb.start()

                    var retStr: String = "";
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        retStr = reader.readText()
                    }

                    val retCode = process.waitFor()

                    if (retCode != 0) {
                        ImageInfo(5, retStr)
                    } else {
                        val patternStr = Pattern.quote(srcFile.absolutePath) + "\\s\\w+\\s\\d+x\\d+=>(\\d+)x(\\d+).*"
                        val pattern = Pattern.compile(patternStr)
                        val matcher = pattern.matcher(retStr)

                        val destImgWid: Int
                        val destImgHei: Int

                        if (matcher.find()) {
                            destImgWid = matcher.group(1).toInt()
                            destImgHei = matcher.group(2).toInt()
                        }
                        else {
                            val patternStr1 = Pattern.quote(srcFile.absolutePath) + "\\s\\w+\\s(\\d+)x(\\d+).*"
                            val pattern1 = Pattern.compile(patternStr1)
                            val matcher1 = pattern1.matcher(retStr)

                            if (matcher1.find()) {
                                destImgWid = matcher1.group(1).toInt()
                                destImgHei = matcher1.group(2).toInt()
                            }
                            else {
                                destImgWid = 0
                                destImgHei = 0
                            }
                        }

                        val destUrl = "${baseUrl}/assets/${destFilePath}"

                        val newUserImgFile = UserImageFile()
                        newUserImgFile.type = "jpg"
                        newUserImgFile.fileName = destFileName
                        newUserImgFile.width = destImgWid
                        newUserImgFile.height = destImgHei
                        newUserImgFile.sessionId = sessionId
                        newUserImgFile.uploadTime = Calendar.getInstance()
                        newUserImgFile.userId = userImgFile.userId

                        userImageFileDao.save(newUserImgFile)

                        ImageInfo(0, null, newUserImgFile.id, destImgWid, destImgHei, destUrl)
                    }
                }
        )
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
            val context = env.getContext<HashMap<String, Any>>()
            val baseUrl = context["baseUrl"]
            "${baseUrl}/assets/user/${imageFile.userId}/${imageFile.sessionId}/${imageFile.fileName}.${imageFile.type}"
        }
    }

    override fun getImageUrl(baseUrl: String, userImgFile: UserImageFile): String {
        return "${baseUrl}/assets/user/${userImgFile.userId}/${userImgFile.sessionId}/${userImgFile.fileName}.${userImgFile.type}"
    }
}


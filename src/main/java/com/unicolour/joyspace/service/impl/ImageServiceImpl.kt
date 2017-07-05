package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dto.ImageInfo
import com.unicolour.joyspace.dto.productToDTO
import com.unicolour.joyspace.service.ImageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

@Service
class ImageServiceImpl : ImageService {
    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    override fun uploadImage(sessionId: String, thumbMaxWidth: Int, thumbMaxHeight: Int, imgFile: MultipartFile?, baseUrl: String): ImageInfo {
        val fileName = UUID.randomUUID().toString().replace("-", "")
        val file = File(assetsDir, "/user/${sessionId}/${fileName}")
        val thumbFile = File(assetsDir, "/user/${sessionId}/${fileName}.thumb.jpg")
        file.parentFile.mkdirs()

        imgFile?.transferTo(file)

        val img = ImageIO.read(file)
        val imgWid = img.width
        val imgHei = img.height

        val scale = minOf(thumbMaxWidth.toDouble() / imgWid.toDouble(), thumbMaxHeight.toDouble() / imgHei.toDouble())
        val thumbWid = (imgWid * scale).toInt()
        val thumbHei = (imgHei * scale).toInt()

        val thumbImg = BufferedImage(thumbWid, thumbHei, BufferedImage.TYPE_INT_RGB)
        val g = thumbImg.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.drawImage(img, 0, 0, thumbWid, thumbHei, null)
        g.dispose()

        ImageIO.write(thumbImg, "jpg", thumbFile)

        val thumbUrl = "${baseUrl}/assets/user/${sessionId}/${fileName}.thumb.jpg"

        return ImageInfo(imgWid, imgHei, thumbUrl, thumbWid, thumbHei)
    }
}
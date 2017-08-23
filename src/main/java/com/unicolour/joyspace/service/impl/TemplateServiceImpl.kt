package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.controller.api.PreviewParam
import com.unicolour.joyspace.dao.UserImageFileDao
import com.unicolour.joyspace.dao.UserLoginSessionDao
import com.unicolour.joyspace.dto.TemplateImageInfo
import com.unicolour.joyspace.dto.TemplateInfo
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.model.UserImageFile
import com.unicolour.joyspace.model.UserLoginSession
import com.unicolour.joyspace.service.ImageService
import com.unicolour.joyspace.service.TemplateService
import org.apache.batik.apps.rasterizer.DestinationType
import org.apache.batik.apps.rasterizer.SVGConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.Color
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Service
class TemplateServiceImpl : TemplateService {


    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    @Autowired
    lateinit var userLoginSessionDao: UserLoginSessionDao

    @Autowired
    lateinit var userImageFileDao: UserImageFileDao

    @Autowired
    lateinit var imageService : ImageService

    @Autowired
    lateinit var transactionTemplate: TransactionTemplate

    private fun toMM(value:String) : Double {
        if (value.endsWith("mm")) {
            return value.substring(0, value.length-2).toDouble()
        }
        else if (value.endsWith("pt")) {
            return value.substring(0, value.length-2).toDouble() / 72.0 * 25.4
        }
        else if (value.endsWith("pc")) {
            return value.substring(0, value.length-2).toDouble() / 72.0 * 25.4 * 12.0
        }
        else if (value.endsWith("cm")) {
            return value.substring(0, value.length-2).toDouble() * 10
        }
        else if (value.endsWith("in")) {
            return value.substring(0, value.length-2).toDouble() * 25.4
        }
        else {  //缺省作为mm处理
            return value.substring(0, value.length).toDouble()
        }
    }

    override fun getTemplateInfo(templateName: String): TemplateInfo? {
        try {
            val templateFile = File(assetsDir, "template/$templateName/template.svg")
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true

            val doc = factory.newDocumentBuilder().parse(templateFile)
            val tplWid = toMM(doc.documentElement.getAttribute("width"))
            val tplHei = toMM(doc.documentElement.getAttribute("height"))
            val userImages = TreeMap<String, TemplateImageInfo>()

            eachImageElement(doc, {imgEle, title, desc ->
                if (desc == "UserImage" || desc == "用户图片" && !userImages.containsKey(title)) {
                    val imgWid = toMM(imgEle.getAttribute("width"))
                    val imgHei = toMM(imgEle.getAttribute("height"))
                    userImages.put(title, TemplateImageInfo(title, imgWid, imgHei))
                }
            })

            return TemplateInfo(tplWid, tplHei, userImages.size, userImages.values.toList())
        }
        catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    private fun eachImageElement(doc: Document, imgEleCallback: (imgEle:Element, title: String, desc: String) -> Unit) {
        val imgElements = doc.getElementsByTagName("image")

        for (i in 0..imgElements.length - 1) {
            val imgEle = imgElements.item(i) as? Element

            var title = ""
            var desc = ""

            if (imgEle != null) {
                val children = imgEle.childNodes
                for (j in 0..children.length - 1) {
                    val child = children.item(j)
                    if (child is Element) {
                        if (child.tagName == "title") {
                            title = child.textContent
                        }
                        else if (child.tagName == "desc") {
                            desc = child.textContent
                        }
                    }
                }

                imgEleCallback.invoke(imgEle, title, desc)
            }
        }
    }

    override fun createPreview(previewParam: PreviewParam, templateName: String, baseUrl: String): TemplatePreviewResult {
        val session = userLoginSessionDao.findOne(previewParam.sessionId)

        if (session == null) {
            return TemplatePreviewResult(1, "用户未登录")
        }
        else {
            val userImgFiles = previewParam.images.map { userImageFileDao.findOne(it.imageId) }
            if (userImgFiles.any { it == null }) {
                return TemplatePreviewResult(2, "没有找到指定ID对应的图片")
            }
            else if (userImgFiles.any { it.userId != session.userId }) {
                return TemplatePreviewResult(3, "不是此用户的图片")
            }

            val templateFile = File(assetsDir, "template/$templateName/template.svg")
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true

            val doc = factory.newDocumentBuilder().parse(templateFile)
            val tplWid = toMM(doc.documentElement.getAttribute("width"))
            val tplHei = toMM(doc.documentElement.getAttribute("height"))

            eachImageElement(doc, {imgEle, title, desc ->
                val xLinkNameSpace = "http://www.w3.org/1999/xlink"
                if (desc == "UserImage" || desc == "用户图片") {
                    var found = false
                    val prevImg = previewParam.images.firstOrNull { it.name == title }
                    if (prevImg != null) {
                        val userImgFile = userImgFiles.firstOrNull { it.id == prevImg.imageId }
                        if (userImgFile != null) {
                            val userImgUrl = imageService.getImageUrl(baseUrl, userImgFile)
                            imgEle.setAttributeNS(
                                    xLinkNameSpace,
                                    "xlink:href",
                                    userImgUrl)
                            found = true
                        }
                    }

                    if (!found) {
                        imgEle.setAttributeNS(xLinkNameSpace, "xlink:href", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAHWlUWHRDb21tZW50AAAAAABDcmVhdGVkIHdpdGggR0lNUGQuZQcAAAANSURBVAjXY+jo6PgPAAXMApjJRsHmAAAAAElFTkSuQmCC")
                    }
                }
                else {
                    val imgSrc = imgEle.getAttributeNS(xLinkNameSpace, "href")
                    if (!imgSrc.startsWith("data:")) {
                        imgEle.setAttributeNS(
                                xLinkNameSpace,
                                "xlink:href",
                                "${baseUrl}/assets/template/$templateName/$imgSrc")
                    }
                }
            })

            val destFileName = UUID.randomUUID().toString().replace("-", "")
            val destSvgFilePath = "user/${session.userId}/${previewParam.sessionId}/${destFileName}.svg"
            val svgUrl = "${baseUrl}/assets/${destSvgFilePath}"
            val destSvgFile = File(assetsDir, destSvgFilePath)

            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.transform(DOMSource(doc), StreamResult(destSvgFile))

            val destJpgFilePath = "user/${session.userId}/${previewParam.sessionId}/${destFileName}.jpg"
            val jpgUrl = "${baseUrl}/assets/${destJpgFilePath}"
            val destImgFile = File(assetsDir, destJpgFilePath)

            val scale = minOf(500.0 / tplWid, 500.0 / tplHei)
            val destJpgWid = (tplWid * scale).toInt()
            val destJpgHei = (tplHei * scale).toInt()

            val svgConverter = SVGConverter()
            svgConverter.setSources(arrayOf(destSvgFile.absolutePath))
            svgConverter.destinationType = DestinationType.JPEG
            svgConverter.quality = 0.9f
            svgConverter.dst = destImgFile
            svgConverter.width = destJpgWid.toFloat()
            svgConverter.height = destJpgHei.toFloat()
            svgConverter.backgroundColor = Color.WHITE
            svgConverter.execute()

            transactionTemplate.execute { saveImgFileRecord(destFileName, destJpgWid, destJpgHei, session) }

            return TemplatePreviewResult(0, null, svgUrl, jpgUrl)
        }
    }

    private fun saveImgFileRecord(destFileName: String,
                                  destJpgWid: Int,
                                  destJpgHei: Int,
                                  session: UserLoginSession) {
        val userJpgImgFile = UserImageFile()
        userJpgImgFile.type = "jpg"
        userJpgImgFile.fileName = destFileName
        userJpgImgFile.width = destJpgWid
        userJpgImgFile.height = destJpgHei
        userJpgImgFile.sessionId = session.id
        userJpgImgFile.uploadTime = Calendar.getInstance()
        userJpgImgFile.userId = session.userId

        val userSvgImgFile = UserImageFile()
        userSvgImgFile.type = "svg"
        userSvgImgFile.fileName = destFileName
        userSvgImgFile.width = 0
        userSvgImgFile.height = 0
        userSvgImgFile.sessionId = session.id
        userSvgImgFile.uploadTime = Calendar.getInstance()
        userSvgImgFile.userId = session.userId

        userImageFileDao.save(userSvgImgFile)
        userImageFileDao.save(userJpgImgFile)
    }

    override fun getTemplateNames(): List<String> {
        val templateDir = File(assetsDir, "template")
        return templateDir.listFiles().filter { it.isDirectory }.map { it.name }
    }
}
package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.controller.api.PreviewParam

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ImageService
import com.unicolour.joyspace.service.TemplateService
import graphql.schema.DataFetcher
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.apps.rasterizer.DestinationType
import org.apache.batik.apps.rasterizer.SVGConverter
import org.apache.batik.gvt.*
import org.apache.batik.util.XMLResourceDescriptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.multipart.MultipartFile
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.Color
import java.awt.geom.AffineTransform
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.transaction.Transactional
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

const val X_LINK_NAMESPACE: String = "http://www.w3.org/1999/xlink"

@Service
open class TemplateServiceImpl : TemplateService {

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

    @Autowired
    lateinit var templateDao: TemplateDao

    @Autowired
    lateinit var templateImageInfoDao: TemplateImageInfoDao

    @Autowired
    lateinit var productDao: ProductDao

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

    @Transactional
    override fun createTemplate(name: String, type: ProductType, templateFile: MultipartFile) {
        val tpl = Template()
        tpl.currentVersion = 1
        tpl.minImageCount = 0
        tpl.name = name
        tpl.type = type.value
        tpl.width = 0.0
        tpl.height = 0.0
        tpl.uuid = UUID.randomUUID().toString().replace("-", "")

        templateDao.save(tpl)

        saveTemplateFiles(tpl, templateFile)
    }

    private fun saveTemplateFiles(tpl: Template, templateFile: MultipartFile) {
        val productionTplPackFile = File(assetsDir, "template/production/${tpl.id}_v${tpl.currentVersion}_${tpl.uuid}.zip")
        productionTplPackFile.parentFile.mkdirs()

        templateFile.inputStream.use {
            Files.copy(it, productionTplPackFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        val tplDir = File(assetsDir, "template/preview/${tpl.id}_v${tpl.currentVersion}")

        ZipInputStream(productionTplPackFile.inputStream()).use {
            var entry: ZipEntry? = it.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    var isTplSvgFile = false
                    var fileName = entry.name
                    if (fileName.toLowerCase().endsWith(".svg")) {
                        fileName = "template.svg"
                        isTplSvgFile = true
                    }

                    val targetFile = File(tplDir, fileName)
                    targetFile.parentFile.mkdirs()
                    targetFile.outputStream().use { out -> it.copyTo(out) }

                    if (isTplSvgFile) {
                        updateTemplateInfo(tpl, targetFile)
                    }
                    else if (isImgFile(fileName)) {   //缩小图片
                        val pb = ProcessBuilder("magick", "mogrify", "-resize", "500x500>", targetFile.absolutePath)    //如果宽度或高度大于1000， 才缩小图片
                        val process = pb.start()
                        val retCode = process.waitFor()

                        if (retCode != 0) {
                            throw IOException("缩小图片失败, 文件名: $fileName")
                        }
                    }
                }
                entry = it.nextEntry
            }
        }
    }

    private fun isImgFile(fileName: String): Boolean {
        val name = fileName.toLowerCase()
        return name.endsWith(".jpg") ||
                name.endsWith(".jpeg") ||
                name.endsWith(".png") ||
                name.endsWith(".bmp") ||
                name.endsWith(".tif") ||
                name.endsWith(".tiff")
    }

    @Transactional
    override fun updateTemplate(id: Int, name: String, type: ProductType, templateFile: MultipartFile?): Boolean {
        val tpl = templateDao.findOne(id)

        if (templateFile == null) {
            tpl.name = name
            tpl.type = type.value

            templateDao.save(tpl)

            return true
        }
        else {
            if (tpl != null) {
                tpl.currentVersion++

                saveTemplateFiles(tpl, templateFile)

                val products = productDao.findByTemplateId(tpl.id)
                products.forEach { it.version++ }
                productDao.save(products)

                return true
            } else {
                return false
            }
        }
    }

    private fun visitImageNodes(node: GraphicsNode, callback: (ImageNode) -> Unit) {
        if (node is ImageNode) {
            callback(node)
        }
        else if (node is CompositeGraphicsNode) {
            node.children.forEach { visitImageNodes(it as GraphicsNode, callback) }
        }
    }

    private fun updateTemplateInfo(template: Template, tplSvgFile: File) {
        val parser = XMLResourceDescriptor.getXMLParserClassName()
        val df = SAXSVGDocumentFactory(parser)
        val doc = df.createSVGDocument(tplSvgFile.toURI().toURL().toString())
        val userAgent = org.apache.batik.bridge.UserAgentAdapter()
        val loader = org.apache.batik.bridge.DocumentLoader(userAgent)
        val ctx = org.apache.batik.bridge.BridgeContext(userAgent, loader)
        ctx.setDynamicState(org.apache.batik.bridge.BridgeContext.DYNAMIC)
        val builder = org.apache.batik.bridge.GVTBuilder()
        val rootGraphicsNode = builder.build(ctx, doc)

        template.width = toMM(doc.documentElement.getAttribute("width"))
        template.height = toMM(doc.documentElement.getAttribute("height"))

        val userImages = ArrayList<TemplateImageInfo>()

        visitImageNodes(rootGraphicsNode, {
            val element = ctx.getElement(it)
            val bounds = it.geometryBounds

            val tplImg = TemplateImageInfo()
            tplImg.template = template

            val transform = calcNodeTransform(it)
            val transformedBounds = it.getTransformedGeometryBounds(transform)

            tplImg.wid = bounds.width
            tplImg.hei = bounds.height
            tplImg.x = bounds.x
            tplImg.y = bounds.y

            tplImg.tw = transformedBounds.width
            tplImg.th = transformedBounds.height
            tplImg.tx = transformedBounds.x
            tplImg.ty = transformedBounds.y

            tplImg.href = element.getAttributeNS(X_LINK_NAMESPACE, "href")

            val matrix = DoubleArray(6)
            transform.getMatrix(matrix)
            tplImg.matrix = matrix.joinToString(",")

            var desc = ""

            val children = element.childNodes
            (0 until children.length)
                    .map { children.item(it) }
                    .filterIsInstance<Element>()
                    .forEach {
                        if (it.tagName == "title") {
                            tplImg.name = it.textContent
                        } else if (it.tagName == "desc") {
                            desc = it.textContent
                        }
                    }

            tplImg.userImage = (desc == "UserImage" || desc == "用户图片")
            userImages.add(tplImg)
        })

        template.minImageCount = userImages.filter { it.userImage }.map { it.name }.toSet().size

        templateDao.save(template)

        templateImageInfoDao.deleteByTemplateId(template.id)
        templateImageInfoDao.save(userImages)
    }

    private fun calcNodeTransform(gn: GraphicsNode): AffineTransform
    {
        val ctm = AffineTransform()
        var node: GraphicsNode? = gn
        while (node != null && !(node is CanvasGraphicsNode)) {
            if (node.transform != null) {
                ctm.preConcatenate(node.transform)
            }
            node = node.parent
        }

        return ctm
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

    override fun createPreview(previewParam: PreviewParam, template: Template, baseUrl: String): TemplatePreviewResult {
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

            val templateFile = File(assetsDir, "template/preview/${template.id}_v${template.currentVersion}/template.svg")
            val factory = DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = true

            val doc = factory.newDocumentBuilder().parse(templateFile)
            val tplWid = toMM(doc.documentElement.getAttribute("width"))
            val tplHei = toMM(doc.documentElement.getAttribute("height"))

            eachImageElement(doc, {imgEle, title, desc ->
                if (desc == "UserImage" || desc == "用户图片") {
                    var found = false
                    val prevImg = previewParam.images.firstOrNull { it.name == title }
                    if (prevImg != null) {
                        val userImgFile = userImgFiles.firstOrNull { it.id == prevImg.imageId }
                        if (userImgFile != null) {
                            val userImgUrl = imageService.getImageUrl(baseUrl, userImgFile)
                            imgEle.setAttributeNS(
                                    X_LINK_NAMESPACE,
                                    "xlink:href",
                                    userImgUrl)
                            found = true
                        }
                    }

                    if (!found) {
                        imgEle.setAttributeNS(X_LINK_NAMESPACE, "xlink:href", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAHWlUWHRDb21tZW50AAAAAABDcmVhdGVkIHdpdGggR0lNUGQuZQcAAAANSURBVAjXY+jo6PgPAAXMApjJRsHmAAAAAElFTkSuQmCC")
                    }
                }
                else {
                    val imgSrc = imgEle.getAttributeNS(X_LINK_NAMESPACE, "href")
                    if (!imgSrc.startsWith("data:")) {
                        imgEle.setAttributeNS(
                                X_LINK_NAMESPACE,
                                "xlink:href",
                                "${baseUrl}/assets/template/preview/${template.id}_v${template.currentVersion}/$imgSrc")
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

    override fun getDataFetcher(fieldName: String): DataFetcher<Any> {
        return DataFetcher<Any> { env ->
            val tplImg = env.getSource<TemplateImageInfo>()
            when (fieldName) {
                "url" -> {
                    val context = env.getContext<HashMap<String, Any>>()
                    val baseUrl = context["baseUrl"]
                    val tpl = tplImg.template
                    val path = tplImg.href?.replace('\\', '/')

                    "${baseUrl}/assets/template/preview/${tpl.id}_v${tpl.currentVersion}/${path}"
                }
                else -> null
            }
        }
    }
}
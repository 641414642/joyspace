package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.controller.api.ImageParam
import com.unicolour.joyspace.controller.api.PreviewParam
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.dto.TemplatePreviewResult
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.ImageService
import com.unicolour.joyspace.service.PrintStationService
import com.unicolour.joyspace.service.TemplateService
import graphql.schema.DataFetcher
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.apps.rasterizer.DestinationType
import org.apache.batik.apps.rasterizer.SVGConverter
import org.apache.batik.gvt.CanvasGraphicsNode
import org.apache.batik.gvt.CompositeGraphicsNode
import org.apache.batik.gvt.GraphicsNode
import org.apache.batik.gvt.ImageNode
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
import kotlin.collections.HashMap

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

    @Autowired
    lateinit var printStationService: PrintStationService

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

        for (i in 0 until imgElements.length) {
            val imgEle = imgElements.item(i) as? Element

            var title = ""
            var desc = ""

            if (imgEle != null) {
                val children = imgEle.childNodes
                for (j in 0 until children.length) {
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
            val svgElement = doc.documentElement
            val tplWid = toMM(svgElement.getAttribute("width"))
            val tplHei = toMM(svgElement.getAttribute("height"))

            var defsElement = getChildElementByName(svgElement, "defs")
            if (defsElement == null) {
                defsElement = doc.createElement("defs")
                val firstChild = svgElement.firstChild
                if (firstChild != null) {
                    svgElement.insertBefore(defsElement, firstChild)
                } else {
                    svgElement.appendChild(defsElement)
                }
            }

            val imgEleUrlMap = HashMap<Element, String>()

            eachImageElement(doc, {imgEle, title, desc ->
                if (desc == "UserImage" || desc == "用户图片") {
                    var found = false
                    val prevImg = previewParam.images.firstOrNull { it.name == title }
                    if (prevImg != null) {
                        val userImgFile = userImgFiles.firstOrNull { it.id == prevImg.imageId }
                        if (userImgFile != null) {
                            val userImgUrl = imageService.getImageUrl(baseUrl, userImgFile)
                            val userImgFileUrl =  imageService.getImageFileUrl(userImgFile)

                            val newImgEle = replaceImageElementWithPattern(defsElement!!, imgEle, userImgFileUrl, userImgFile.width, userImgFile.height, prevImg)
                            found = true

                            imgEleUrlMap[newImgEle] = userImgUrl
                        }
                    }

                    if (!found) {
                        imgEle.setAttributeNS(X_LINK_NAMESPACE, "xlink:href", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAHWlUWHRDb21tZW50AAAAAABDcmVhdGVkIHdpdGggR0lNUGQuZQcAAAANSURBVAjXY+jo6PgPAAXMApjJRsHmAAAAAElFTkSuQmCC")
                    }
                }
                else {
                    val imgSrc = imgEle.getAttributeNS(X_LINK_NAMESPACE, "href")
                    if (!imgSrc.startsWith("data:")) {
                        val imgUrl = "${baseUrl}/assets/template/preview/${template.id}_v${template.currentVersion}/$imgSrc"
                        val imgFileUrl = File(assetsDir, "/template/preview/${template.id}_v${template.currentVersion}/$imgSrc").toURI().toURL().toExternalForm()

                        imgEle.setAttributeNS(X_LINK_NAMESPACE, "xlink:href", imgFileUrl)
                        imgEleUrlMap[imgEle] = imgFileUrl
                    }
                }
            })

            //生成预览图用的svg文件, 图片url为 file:///xxx.xxx.....
            val destFileName = UUID.randomUUID().toString().replace("-", "")
            val svgFilePath = "user/${session.userId}/${previewParam.sessionId}/${destFileName}.svg"
            val svgFile = File(assetsDir, svgFilePath)

            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.transform(DOMSource(doc), StreamResult(svgFile))

            val destJpgFilePath = "user/${session.userId}/${previewParam.sessionId}/${destFileName}.jpg"
            val jpgUrl = "${baseUrl}/assets/${destJpgFilePath}"
            val destImgFile = File(assetsDir, destJpgFilePath)

            val scale = minOf(1000.0 / tplWid, 1000.0 / tplHei)
            val destJpgWid = (tplWid * scale).toInt()
            val destJpgHei = (tplHei * scale).toInt()

            val svgConverter = SVGConverter()
            svgConverter.setSources(arrayOf(svgFile.absolutePath))
            svgConverter.destinationType = DestinationType.JPEG
            svgConverter.quality = 0.9f
            svgConverter.dst = destImgFile
            svgConverter.width = destJpgWid.toFloat()
            svgConverter.height = destJpgHei.toFloat()
            svgConverter.backgroundColor = Color.WHITE
            svgConverter.execute()

            transactionTemplate.execute { saveImgFileRecord(destFileName, destJpgWid, destJpgHei, session) }

            //客户端预览用的svg文件, 图片url为 https://xxx.xxx.....
            for ((imgEle, imgUrl) in imgEleUrlMap) {
                imgEle.setAttributeNS(X_LINK_NAMESPACE, "xlink:href", imgUrl)
            }
            val svgUrl = "${baseUrl}/assets/${svgFilePath}"
            transformer.transform(DOMSource(doc), StreamResult(svgFile))

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

    override val templateFileUrlDataFetcher: DataFetcher<String?>
        get() {
            return DataFetcher { env ->
                val sessionId = env.getArgument<String>("sessionId")
                val templateId = env.getArgument<Int>("templateId")
                val templateVersion = env.getArgument<Int>("templateVersion")

                val loginSession = printStationService.getPrintStationLoginSession(sessionId)
                if (loginSession == null) {
                    null
                }
                else {
                    val template = templateDao.findOne(templateId)
                    if (template == null) {
                        null
                    }
                    else {
                        val context = env.getContext<HashMap<String, Any>>()
                        val baseUrl = context["baseUrl"]

                        "${baseUrl}/assets/template/production/${template.id}_v${templateVersion}_${template.uuid}.zip"
                    }
                }
            }
        }
    override val templatesDataFetcher: DataFetcher<List<Template>>
        get() {
            return DataFetcher { env ->
                val sessionId = env.getArgument<String>("sessionId")
                val loginSession = printStationService.getPrintStationLoginSession(sessionId)
                if (loginSession == null) {
                    emptyList<Template>()
                }
                else {
                    templateDao.findAll().toList()
                }
            }
        }

    override fun getTemplateImageDataFetcher(fieldName: String): DataFetcher<Any> {
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

    private fun getChildElementByName(element: Element, childElementTagName: String): Element? {
        val children = element.childNodes
        for (i in 0 until children.length) {
            val childNode = children.item(i)
            if (childNode is Element && childNode.tagName == childElementTagName) {
                return childNode
            }
        }

        return null
    }

    private fun getDoubleAttribute(element: Element, attrName: String, defaultValue: Double): Double {
        val attrValue = element.getAttribute(attrName)
        return if (attrValue.isNullOrEmpty()) {
            defaultValue
        } else {
            return attrValue.toDoubleOrNull() ?: defaultValue
        }
    }

    private fun createElement(doc: Document, parentElement: Element?, tagName: String, vararg attrNameAndValues: String): Element {
        val element = doc.createElement(tagName)
        var i = 0
        while (i < attrNameAndValues.size) {
            element.setAttribute(attrNameAndValues[i], attrNameAndValues[i + 1])
            i += 2
        }
        parentElement?.appendChild(element)
        return element
    }

    //返回pattern里面的新创建的image element
    private fun replaceImageElementWithPattern(
            defsElement: Element, imageElement: Element,
            imagePath: String, imageWidth: Int, imageHeight: Int,
            imageParam: ImageParam): Element {

        val doc = defsElement.ownerDocument

        //create filter element
        var filterId: String? = null
        if (imageParam.brightness != 1.0 || imageParam.saturate != 1.0 || imageParam.effect != "none") {
            filterId = "filter_" + UUID.randomUUID().toString().replace("-", "")
            val filterElement = createElement(doc, defsElement, "filter", "id", filterId)

            if (imageParam.brightness != 1.0) {
                val feComponentTransferElement = createElement(doc, filterElement, "feComponentTransfer")
                createElement(doc, feComponentTransferElement, "feFuncR", "type", "linear", "slope", imageParam.brightness.toString())
                createElement(doc, feComponentTransferElement, "feFuncG", "type", "linear", "slope", imageParam.brightness.toString())
                createElement(doc, feComponentTransferElement, "feFuncB", "type", "linear", "slope", imageParam.brightness.toString())
            }

            if (imageParam.saturate != 1.0) {
                createElement(doc, filterElement, "feColorMatrix",
                        "type", "saturate",
                        "values", imageParam.saturate.toString())
            }

            if (imageParam.effect == "sepia") {
                val matrixValues = "0.393 0.769 0.189 0 0 0.349 0.686 0.168 0 0 0.272 0.534 0.131 0 0 0 0 0 1 0"
                createElement(doc, filterElement, "feColorMatrix", "type", "matrix", "values", matrixValues)
            } else if (imageParam.effect == "grayscale") {
                val matrixValues = "0.2126 0.7152 0.0722 0 0 0.2126 0.7152 0.0722 0 0 0.2126 0.7152 0.0722 0 0 0 0 0 1 0"
                createElement(doc, filterElement, "feColorMatrix", "type", "matrix", "values", matrixValues)
            }
        }

        val x = getDoubleAttribute(imageElement, "x", 0.0)
        val y = getDoubleAttribute(imageElement, "y", 0.0)
        val w = getDoubleAttribute(imageElement, "width", 0.0)
        val h = getDoubleAttribute(imageElement, "height", 0.0)

        //create pattern element
        val patternId = "pattern_" + UUID.randomUUID().toString().replace("-", "")
        val patternElement = createElement(doc, defsElement, "pattern",
                "patternUnits", "userSpaceOnUse",
                "patternContentUnits", "userSpaceOnUse",
                "x", x.toString(),
                "y", y.toString(),
                "width", w.toString(),
                "height", h.toString(),
                "id", patternId)

        //create pattern white background element
        createElement(doc, patternElement, "rect",
                "x", "0",
                "y", "0",
                "width", w.toString(),
                "height", h.toString(),
                "style", "fill:#ffffff;fill-opacity:1")

        //create pattern image element
        val patternImgElement = createElement(doc, patternElement, "image",
                "x", (-imageWidth / 2.0).toString(),
                "y", (-imageHeight / 2.0).toString(),
                "width", imageWidth.toString(),
                "height", imageHeight.toString(),
                "preserveAspectRatio", "none")
        patternImgElement.setAttributeNS(X_LINK_NAMESPACE, "xlink:href", imagePath)
        if (filterId != null) {
            patternImgElement.setAttribute("filter", "url(#$filterId)")
        }

        val transform = AffineTransform()
        transform.translate(w / 2.0, h / 2.0)   //坐标原点移到图片框中心位置

        //用户平移
        if (imageParam.horTranslate != 0.0 || imageParam.verTranslate != 0.0) {
            transform.translate(imageParam.horTranslate, imageParam.verTranslate)
        }

        //用户缩放
        var s = imageParam.scale
        //先缩放图片到充满pattern
        val iw = if (imageParam.initialRotate == 90 || imageParam.initialRotate == 270) imageHeight else imageWidth
        val ih = if (imageParam.initialRotate == 90 || imageParam.initialRotate == 270) imageWidth else imageHeight
        s *= Math.max(w / iw, h / ih)

        if (s != 1.0) {
            transform.scale(s, s)
        }

        //用户旋转
        if (imageParam.initialRotate + imageParam.rotate != 0.0) {
            transform.rotate(Math.toRadians(imageParam.initialRotate + imageParam.rotate))
        }

        val matrix = DoubleArray(6)
        transform.getMatrix(matrix)
        patternImgElement.setAttribute("transform", matrix.joinToString(",", "matrix(", ")"))

        //create rect element to replace original image element
        val rectElement = createElement(doc, null, "rect",
                "x", x.toString(),
                "y", y.toString(),
                "width", w.toString(),
                "height", h.toString(),
                "style", "fill:url(#$patternId);stroke:none;")
        val originImgEleTransform = imageElement.getAttribute("transform")
        if (originImgEleTransform != null && originImgEleTransform != "") {
            rectElement.setAttribute("transform", originImgEleTransform)
        }

        imageElement.parentNode.replaceChild(rectElement, imageElement)

        return patternImgElement
    }
}
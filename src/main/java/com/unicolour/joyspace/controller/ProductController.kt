package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.ProductImageFileDao
import com.unicolour.joyspace.dao.TemplateDao
import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.model.ProductImageFileType
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.service.TemplateService
import com.unicolour.joyspace.util.Pager
import com.unicolour.joyspace.util.getBaseUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

@Controller
class ProductController {

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var productImageFileDao: ProductImageFileDao

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var templateService: TemplateService

    @Autowired
    lateinit var templateDao: TemplateDao

    @RequestMapping("/product/list")
    fun productList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20)
        val products = if (name == null || name == "")
            productDao.findAll(pageable)
        else
            productDao.findByName(name, pageable)

        modelAndView.model.put("inputProductName", name)

        val pager = Pager(products.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("products", products.content)

        modelAndView.model.put("viewCat", "product_mgr")
        modelAndView.model.put("viewContent", "product_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/edit"), method = arrayOf(RequestMethod.GET))
    fun editProduct(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var product: Product? = null
        if (id > 0) {
            product = productDao.findOne(id)
        }

        if (product == null) {
            product = Product()
        }

        modelAndView.model["templates"] = templateDao.findAll()

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("product", product)
        modelAndView.viewName = "/product/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editProduct(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "remark", required = true) remark: String,
            @RequestParam(name = "defPrice", required = true) defPrice: Double,
            @RequestParam(name = "templateId", required = true) templateId: Int
    ): Boolean {

        if (id <= 0) {
            productService.createProduct(name, remark, defPrice, templateId)
            return true
        } else {
            return productService.updateProduct(id, name, remark, defPrice, templateId)
        }
    }

    @RequestMapping(path = arrayOf("/product/manageImageFiles"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun manageImageFiles(
            request: HttpServletRequest,
            modelAndView: ModelAndView,
            @RequestParam(name = "productId", required = true) productId: Int): ModelAndView {

        val baseUrl = getBaseUrl(request)

        modelAndView.model.put("productId", productId)

        val thumbImages = productImageFileDao.findByProductIdAndType(productId = productId, type = ProductImageFileType.THUMB.value)
        val prevImages = productImageFileDao.findByProductIdAndType(productId = productId, type = ProductImageFileType.PREVIEW.value)

        modelAndView.model.put("thumbImages", thumbImages.map {
            PreviewImageFileDTO("${baseUrl}/assets/product/images/${it.id}.${it.fileType}", it.id)
        })
        modelAndView.model.put("previewImages", prevImages.map {
            PreviewImageFileDTO("${baseUrl}/assets/product/images/${it.id}.${it.fileType}", it.id)
        })

        modelAndView.viewName = "/product/edit :: manageImageFiles"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/uploadImageFile"), method = arrayOf(RequestMethod.POST))
    fun uploadProductImages(
            request: HttpServletRequest,
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "type", required = true) type: String,
            @RequestParam("imageFile") imageFile: MultipartFile?
    ): ModelAndView {
        val baseUrl = getBaseUrl(request)

        val imgType = if (type == "thumb") ProductImageFileType.THUMB else ProductImageFileType.PREVIEW
        val uploadedImgFile = productService.uploadProductImageFile(id, imgType, imageFile)

        if (uploadedImgFile != null) {
            modelAndView.model["imageUploadDivId"] = when (type) {
                "thumb" -> "thumbImgUpload"
                else -> "prevImgUpload"
            }
            modelAndView.viewName = "/product/imageFileUploaded"
            modelAndView.model["uploadedImg"] = PreviewImageFileDTO("${baseUrl}/assets/product/images/${uploadedImgFile.id}.${uploadedImgFile.fileType}", uploadedImgFile.id)
        }

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/deleteImageFile"), method = arrayOf(RequestMethod.POST))
    fun deleteProductImages(
            modelAndView: ModelAndView,
            @RequestParam(name = "imgFileId", required = true) imgFileId: Int): ModelAndView {
        productService.deleteProductImageFile(imgFileId)
        modelAndView.model["imgFileId"] = imgFileId
        modelAndView.viewName = "/product/imageFileDeleted"
        return modelAndView
    }
}

internal class PreviewImageFileDTO(val url: String, val id: Int)
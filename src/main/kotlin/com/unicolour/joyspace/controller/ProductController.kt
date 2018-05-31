package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.ProductImageFileDao
import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.model.ProductImageFileType
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.service.TemplateService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView

@Controller
class ProductController {
    @Value("\${com.unicolour.joyspace.baseUrl}")
    lateinit var baseUrl: String

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var productImageFileDao: ProductImageFileDao

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var templateService: TemplateService

    @RequestMapping("/product/list")
    fun productList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        if (loginManager == null) {
            modelAndView.viewName = "empty"
            return modelAndView
        }

        val products = productService.queryProducts(pageno, 20, loginManager.companyId, name, true,"sequence asc")

        modelAndView.model["inputProductName"] = name

        modelAndView.model["isSuperAdmin"] = managerService.loginManagerHasRole("ROLE_SUPERADMIN")
        modelAndView.model["adminCompanyId"] = loginManager.companyId

        val pager = Pager(products.totalPages, 7, pageno - 1)
        modelAndView.model["pager"] = pager

        modelAndView.model["products"] = products.content

        modelAndView.model["viewCat"] = "product_mgr"
        modelAndView.model["viewContent"] = "product_list"
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

        modelAndView.model["templates"] = templateService.queryTemplates(null, "", true, "id asc")

        modelAndView.model["create"] = id <= 0
        modelAndView.model["product"] = product
        modelAndView.viewName = "/product/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/move"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun moveProduct(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "up", required = true) up: Boolean): Boolean {
        return productService.moveProduct(id, up)
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

        return if (id <= 0) {
            productService.createProduct(name, remark, defPrice, templateId)
            true
        } else {
            productService.updateProduct(id, name, remark, defPrice, templateId)
        }
    }

    @RequestMapping(path = arrayOf("/product/manageImageFiles"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun manageImageFiles(
            modelAndView: ModelAndView,
            @RequestParam(name = "productId", required = true) productId: Int): ModelAndView {

        val product = productDao.findOne(productId)

        modelAndView.model.put("product", product)

        val thumbImages = productImageFileDao.findByProductIdAndType(productId = productId, type = ProductImageFileType.THUMB.value)
        val prevImages = productImageFileDao.findByProductIdAndType(productId = productId, type = ProductImageFileType.PREVIEW.value)

        modelAndView.model["thumbImages"] = thumbImages.map {
            PreviewImageFileDTO("$baseUrl/assets/product/images/${it.id}.${it.fileType}", it.id)
        }
        modelAndView.model["previewImages"] = prevImages.map {
            PreviewImageFileDTO("$baseUrl/assets/product/images/${it.id}.${it.fileType}", it.id)
        }

        modelAndView.viewName = "/product/edit :: manageImageFiles"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/uploadImageFile"), method = arrayOf(RequestMethod.POST))
    fun uploadProductImages(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "type", required = true) type: String,
            @RequestParam("imageFile") imageFile: MultipartFile?
    ): ModelAndView {
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

    @PostMapping("/product/deleteProduct")
    @ResponseBody
    fun deleteProduct(@RequestParam(name = "productId", required = true) productId: Int): Boolean {
        return productService.deleteProductById(productId)
    }
}

internal class PreviewImageFileDTO(val url: String, val id: Int)
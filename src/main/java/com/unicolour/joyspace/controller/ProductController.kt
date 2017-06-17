package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.model.Product
import com.unicolour.joyspace.service.ProductService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView

@Controller
class ProductController {

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var productService: ProductService

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
            @RequestParam(name = "sn", required = true) sn: String,
            @RequestParam(name = "remark", required = true) remark: String,
            @RequestParam(name = "resX", required = true) resX: Int,
            @RequestParam(name = "resY", required = true) resY: Int,
            @RequestParam(name = "defPrice", required = true) defPrice: Double,
            @RequestParam(name = "minImgCount", required = true) minImgCount: Int
    ): Boolean {

        if (id <= 0) {
            productService.createProduct(name, sn, remark, resX, resY, defPrice, minImgCount)
            return true
        } else {
            return productService.updateProduct(id, name, sn, remark, resX, resY, defPrice, minImgCount)
        }
    }

    @RequestMapping(path = arrayOf("/product/uploadImages"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun uploadProductImages(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        modelAndView.model.put("productId", id)
        modelAndView.viewName = "/product/edit :: uploadImageFiles"
        return modelAndView
    }

    @RequestMapping(path = arrayOf("/product/uploadImages"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun uploadProductImages(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam("thumbImgFile") thumbImgFile: MultipartFile?,
            @RequestParam("previewImgFile") previewImgFile: MultipartFile?
    ): ModelAndView {
        productService.uploadProductImageFiles(id, thumbImgFile, previewImgFile)

        modelAndView.viewName = "product/uploaded"
        return modelAndView
    }
}
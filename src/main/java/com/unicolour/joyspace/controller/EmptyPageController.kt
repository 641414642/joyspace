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
class EmptyPageController {
    @RequestMapping("/empty")
    fun emptyPage(modelAndView: ModelAndView): ModelAndView {
        modelAndView.viewName = "empty"
        return modelAndView
    }
}
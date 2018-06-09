package com.unicolour.joyspace.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.ProductDao
import com.unicolour.joyspace.dao.TPriceDao
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.*
//import com.unicolour.joyspace.dao.TPriceItemDao
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.HashMap
import java.util.ArrayList
import javax.servlet.http.HttpServletRequest


@Controller
class TpriceController {

    @Autowired
    lateinit var tPriceService: TPriceService

    @Autowired
    lateinit var tPriceDao: TPriceDao

//    @Autowired
//    lateinit var tPriceItemDao: TPriceItemDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var managerService: ManagerService


    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @RequestMapping("/event/tprice/list")
    fun tpriceList(
            modelAndView: ModelAndView,
            @RequestParam(name = "inputTpriceName", required = false, defaultValue = "") inputTpriceName: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.DESC, "id")
        val tprice_list = if (inputTpriceName == null || inputTpriceName == "")
            tPriceDao.findByCompanyId(loginManager!!.companyId, pageable)
        else
            tPriceDao.findByNameAndCompanyId(inputTpriceName, loginManager!!.companyId, pageable)

        val pager = Pager(tprice_list.totalPages, 7, pageno - 1)

        modelAndView.model.put("inputTpriceName", inputTpriceName)
        modelAndView.model.put("pager", pager)
        modelAndView.model.put("tprice_list", tprice_list.content)
        modelAndView.model["viewCat"] = "event_mgr"
        modelAndView.model["viewContent"] = "tprice_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/event/tprice/show"), method = arrayOf(RequestMethod.GET))
    fun editTemplate(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {

        var tprice: TPrice? = null

        if (id > 0) {

            tprice = tPriceDao.findOne(id)
        }

        if (tprice == null) {

            tprice = TPrice()

            val now = LocalDate.now()
            tprice.begin = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant())
            tprice.expire = Date.from(now.plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant())
        }

        val loginManager = managerService.loginManager

        if (loginManager == null) {

            return modelAndView
        }

        var product_list = productDao.findAllByCompanyId(loginManager.companyId)

        modelAndView.model["create"] = id <= 0
        modelAndView.model["tprice"] = tprice
        modelAndView.model["product_list"] = product_list
        modelAndView.viewName = "/tprice/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/event/tprice/show"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editIdPhotoTemplate(
            request: HttpServletRequest,

            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "begin", required = true) begin: String,
            @RequestParam(name = "expire", required = true) expire: String,
            @RequestParam(name = "product", required = true) product_id: Int,
            @RequestParam(name = "min", required = true) min1: Int,
            @RequestParam(name = "min2", required = false) min2: Int,
            @RequestParam(name = "min3", required = false) min3: Int,
            @RequestParam(name = "min4", required = false) min4: Int,
            @RequestParam(name = "min5", required = false) min5: Int,

            @RequestParam(name = "max1", required = true) max1: Int,
            @RequestParam(name = "max2", required = false) max2: Int,
            @RequestParam(name = "max3", required = false) max3: Int,
            @RequestParam(name = "max4", required = false) max4: Int,
            @RequestParam(name = "max5", required = false) max5: Int,

            @RequestParam(name = "price1", required = true) price1: Int,
            @RequestParam(name = "price2", required = false) price2: Int,
            @RequestParam(name = "price3", required = false) price3: Int,
            @RequestParam(name = "price4", required = false) price4: Int,
            @RequestParam(name = "price5", required = false) price5: Int

    ): Boolean {

        if (min1 <= 0 || max1 <= 0 || price1<=0) {

        }

        val list = ArrayList<TPriceItem>()

        if (min1 >0) {

            if ( max1 < min1){

                return false
            }
            val item1 = TPriceItem()
            item1.minCount = min1
            item1.maxCount = max1
            item1.price = price1
            list.add(item1)
        }

        if (min2 >0) {

            if ( min2 <= max1){

                return false
            }

            val item2 = TPriceItem()
            item2.minCount = min2
            item2.maxCount = max2
            item2.price = price2
            list.add(item2)
        }


        if (min3 >0) {

            if ( min3 <= max2){

                return false
            }

            val item3 = TPriceItem()
            item3.minCount = min3
            item3.maxCount = max3
            item3.price = price3
            list.add(item3)
        }

        if (min4 >0) {

            if ( min4 <= max3){

                return false
            }
            val item4 = TPriceItem()
            item4.minCount = min4
            item4.maxCount = max4
            item4.price = price4
            list.add(item4)
        }

        if (min5 >0) {

            if ( min5 <= max4){

                return false
            }

            val item5 = TPriceItem()
            item5.minCount = min5
            item5.maxCount = max5
            item5.price = price5
            list.add(item5)
        }

        val df = SimpleDateFormat("yyyy-MM-dd")

        return tPriceService.createtp(name, df.parse(begin), df.parse(expire), product_id, list)

    }

}

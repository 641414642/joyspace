package com.unicolour.joyspace.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.*
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.ArrayList
import javax.servlet.http.HttpServletRequest


@Controller
class TpriceController {

    @Autowired
    lateinit var tPriceService: TPriceService

    @Autowired
    lateinit var tPriceDao: TPriceDao

    @Autowired
    lateinit var tPriceItemDao: TPriceItemDao

    @Autowired
    lateinit var productDao: ProductDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var positionDao: PositionDao

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
            tPriceDao.findByCompanyId(loginManager!!.companyId,pageable)
        else
            tPriceDao.findByNameAndCompanyId(inputTpriceName, loginManager!!.companyId,pageable)

        class tprice_items(val tPrice: TPrice,val tproduct: Product,val tposition: Position, val tprice_item: List<tpriceItem>)


        val tprice_lists = tprice_list.map {
            tprice_items(
                tPrice = it,
                    tproduct = productDao.findOne(it.productId),
                    tposition = positionDao.findOne(it.positionId),
                    tprice_item = it.tPriceItems.map { pitem ->
                tpriceItem(
                    id = pitem.id,
                    maxCount = pitem.maxCount,
                    minCount = pitem.minCount,
                    price = pitem.price
                    )
                }
            )
        }

        val pager = Pager(tprice_list.totalPages, 7, pageno - 1)

        modelAndView.model.put("inputTpriceName", inputTpriceName)
        modelAndView.model.put("pager", pager)
        modelAndView.model.put("tprice_list", tprice_lists)
        modelAndView.model["viewCat"] = "event_mgr"
        modelAndView.model["viewContent"] = "tprice_list"
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/event/tprice/show"), method = arrayOf(RequestMethod.GET))
    fun show(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {

        var tprice: TPrice? = null

        if (id > 0) {

            tprice = tPriceDao.findOne(id)

            val tpriceItem = tPriceItemDao.findByTPriceIdOrderByIdAsc(tprice.id)
            val list = ArrayList<TPriceItem>()
            val item = TPriceItem()

            for (nullobj in 0 until 5 - tpriceItem.size){

                item.minCount = 0
                item.maxCount = 0
                item.price = 0
                list.add(item)
            }

            modelAndView.model["tpitemCount"] =  tpriceItem.size
            modelAndView.model["nullobj"] = list
            modelAndView.model["tpitem"] = tpriceItem
            modelAndView.viewName = "/tprice/edit:: content"
        }

        if (tprice == null) {

            tprice = TPrice()

            val now = LocalDate.now()
            tprice.begin = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant())
            tprice.expire = Date.from(now.plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant())

            modelAndView.viewName = "/tprice/create :: content"
        }

        val loginManager = managerService.loginManager
        var product_list = productDao.findByDeleted(false)
        val allPositions = positionDao.findByCompanyId(loginManager!!.companyId)

        modelAndView.model["positions"] = allPositions
        modelAndView.model["product_list"] = product_list
        modelAndView.model["tprice"] = tprice
        modelAndView.model["create"] = id <= 0

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/event/tprice/show"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun edit(
            request: HttpServletRequest,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "begin", required = true) begin: String,
            @RequestParam(name = "expire", required = true) expire: String,
            @RequestParam(name = "product", required = true) product_id: Int,
            @RequestParam(name = "min1", required = true) min1: Int,
            @RequestParam(name = "min2", required = true) min2: Int,
            @RequestParam(name = "min3", required = true) min3: Int,
            @RequestParam(name = "min4", required = true) min4: Int,
            @RequestParam(name = "min5", required = true) min5: Int,
            @RequestParam(name = "max1", required = true) max1: Int,
            @RequestParam(name = "max2", required = true) max2: Int,
            @RequestParam(name = "max3", required = true) max3: Int,
            @RequestParam(name = "max4", required = true) max4: Int,
            @RequestParam(name = "max5", required = true) max5: Int,
            @RequestParam(name = "price1", required = true) price1: Double,
            @RequestParam(name = "price2", required = true) price2: Double,
            @RequestParam(name = "price3", required = true) price3: Double,
            @RequestParam(name = "price4", required = true) price4: Double,
            @RequestParam(name = "price5", required = true) price5: Double,
            @RequestParam(name = "id", required = true) id: Int

    ): Boolean {

        if (min1 <= 0 || max1 <= 0 || price1<=0) {

            return false
        }


        val list = ArrayList<TPriceItem>()

        if (min1 >0) {

            if ( max1 < min1){

                return false
            }

            val tpitem_id = request.getParameter("tpitem1")

            val item1 = TPriceItem()
            item1.minCount = min1
            item1.maxCount = max1
            item1.price = (price1 * 100).toInt()

            if (tpitem_id != null){

                tPriceService.updatetpItem(tpitem_id.toInt(),item1)

            }else{

                list.add(item1)
            }

        }

        if ((min2 != 0) and (max2 !=0)) {

            if ( min2 <= max1){

                return false
            }

            val tpitem_id = request.getParameter("tpitem2")

            val item2 = TPriceItem()
            item2.minCount = min2
            item2.maxCount = max2
            item2.price = (price2 * 100).toInt()

            if (tpitem_id != null){

                tPriceService.updatetpItem(tpitem_id.toInt(),item2)

            }else{

                list.add(item2)
            }
        }


        if ((min3 != 0) and (max3 !=0)) {

            if ( min3 <= max2){

                return false
            }

            val tpitem_id = request.getParameter("tpitem3")

            val item3 = TPriceItem()
            item3.minCount = min3
            item3.maxCount = max3
            item3.price = (price3 * 100).toInt()

            if (tpitem_id != null){

                tPriceService.updatetpItem(tpitem_id.toInt(),item3)

            }else{

                list.add(item3)
            }

        }

        if ((min4 != 0) and (max4 !=0)) {

            if ( min4 <= max3){

                return false
            }

            val tpitem_id = request.getParameter("tpitem4")

            val item4 = TPriceItem()
            item4.minCount = min4
            item4.maxCount = max4
            item4.price = (price4 * 100).toInt()

            if (tpitem_id != null){

                tPriceService.updatetpItem(tpitem_id.toInt(),item4)

            }else{

                list.add(item4)
            }
        }

        if ((min5 != 0) and (max5 !=0)) {

            if ( min5 <= max4){

                return false
            }

            val tpitem_id = request.getParameter("tpitem5")
            val item5 = TPriceItem()
            item5.minCount = min5
            item5.maxCount = max5
            item5.price = (price5 * 100).toInt()

            if (tpitem_id != null){

                tPriceService.updatetpItem(tpitem_id.toInt(),item5)

            }else{

                list.add(item5)
            }
        }

        val df = SimpleDateFormat("yyyy-MM-dd")

        if ( id> 0){

            return tPriceService.updatetp(id,name, df.parse(begin), df.parse(expire), product_id, list)

        }else{

            return tPriceService.createtp(name, df.parse(begin), df.parse(expire), product_id, list)
        }

    }

    @RequestMapping(path = arrayOf("/event/tprice/enable"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun tpriceEnabled(
            @RequestParam(name = "id", required = true) id: Int): Boolean {
        return tPriceService.tpriceEnabled(id)
    }


    class tpriceItem(val id: Int,val maxCount: Int, val minCount: Int, val price: Int)


    class productName(val product_name: String)



}

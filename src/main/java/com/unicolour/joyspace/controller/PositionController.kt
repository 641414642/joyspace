package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PriceListDao
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.service.PositionService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView

@Controller
class PositionController {

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var positionService: PositionService

    @Autowired
    lateinit var priceListDao: PriceListDao

    @RequestMapping("/position/list")
    fun positionList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20)
        val positions = if (name == null || name == "")
            positionDao.findAll(pageable)
        else
            positionDao.findByName(name, pageable)

        modelAndView.model.put("inputPositionName", name)

        val pager = Pager(positions.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("positions", positions.content)

        modelAndView.model.put("viewCat", "product_mgr")
        modelAndView.model.put("viewContent", "position_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/position/edit"), method = arrayOf(RequestMethod.GET))
    fun editPosition(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int
    ): ModelAndView {

        var position: Position? = null
        if (id > 0) {
            position = positionDao.findOne(id)
        }

        if (position == null) {
            position = Position()
        }

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("position", position)
        modelAndView.model.put("priceLists", priceListDao.findAll())
        modelAndView.viewName = "/position/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/position/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editPosition(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "address", required = true) address: String,
            @RequestParam(name = "longitude", required = true) longitude: Double,
            @RequestParam(name = "latitude", required = true) latitude: Double,
            @RequestParam(name = "priceListId", required = true) priceListId: Int
    ): Boolean {

        if (id <= 0) {
            positionService.createPosition(name, address, longitude, latitude, priceListId)
            return true
        } else {
            return positionService.updatePosition(id, name, address, longitude, latitude, priceListId)
        }
    }
}


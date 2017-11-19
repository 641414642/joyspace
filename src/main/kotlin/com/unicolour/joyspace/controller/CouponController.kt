package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.CouponDao
import com.unicolour.joyspace.model.*
import com.unicolour.joyspace.service.CouponService
import com.unicolour.joyspace.util.Pager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Controller
class CouponController {

    @Autowired
    lateinit var couponService: CouponService

    @Autowired
    lateinit var couponDao: CouponDao

    @RequestMapping("/coupon/list")
    fun couponList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.DESC, "id")
        val coupons = if (name == null || name == "")
            couponDao.findAll(pageable)
        else
            couponDao.findByName(name, pageable)

        modelAndView.model.put("inputCouponName", name)

        val pager = Pager(coupons.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("coupons", coupons.content)

        modelAndView.model.put("viewCat", "product_mgr")
        modelAndView.model.put("viewContent", "coupon_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/coupon/edit"), method = arrayOf(RequestMethod.GET))
    fun editCoupon(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {
        var coupon: Coupon? = null
        if (id > 0) {
            coupon = couponDao.findOne(id)
        }

        if (coupon == null) {
            val now = LocalDate.now()
            coupon = Coupon()
            coupon.getMethod = CouponGetMethod.INPUT_CODE.value
            coupon.begin = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant())
            coupon.expire = Date.from(now.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant())
            coupon.minExpense = 100
            coupon.discount = 10
        }

        modelAndView.model["coupons"] = couponDao.findAll()
        modelAndView.model["getMethods"] = CouponGetMethod.values()

        modelAndView.model.put("create", id <= 0)
        modelAndView.model.put("coupon", coupon)
        modelAndView.viewName = "/coupon/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/coupon/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editCoupon(
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "code", required = true) code: String,
            @RequestParam(name = "getMethod", required = true) getMethod: Int,
            @RequestParam(name = "maxUses", required = true) maxUses: Int,
            @RequestParam(name = "maxUsesPerUser", required = true) maxUsesPerUser: Int,
            @RequestParam(name = "minExpense", required = true) minExpense: Int,
            @RequestParam(name = "discount", required = true) discount: Int,
            @RequestParam(name = "begin", required = true) begin: String,
            @RequestParam(name = "expire", required = true) expire: String
    ): Boolean {

        val df = SimpleDateFormat("yyyy-MM-dd")
        val couponGetMethod = CouponGetMethod.values().find{ it.value == getMethod }
        if (id <= 0) {
            couponService.createCoupon(name, code, couponGetMethod!!, maxUses, maxUsesPerUser,
                    minExpense * 100, discount * 100,
                    df.parse(begin), df.parse(expire))
            return true
        } else {
            return couponService.updateCoupon(id, name, code, couponGetMethod!!, maxUses, maxUsesPerUser,
                    minExpense * 100, discount * 100,
                    df.parse(begin), df.parse(expire))
        }
    }
}
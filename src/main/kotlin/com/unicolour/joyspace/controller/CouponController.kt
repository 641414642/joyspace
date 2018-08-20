package com.unicolour.joyspace.controller

import com.unicolour.joyspace.dao.CouponDao
import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dto.*
import com.unicolour.joyspace.model.Coupon
import com.unicolour.joyspace.model.CouponClaimMethod
import com.unicolour.joyspace.model.CouponConstrainsType
import com.unicolour.joyspace.model.ProductType
import com.unicolour.joyspace.service.CouponService
import com.unicolour.joyspace.service.ManagerService
import com.unicolour.joyspace.service.ProductService
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
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
class CouponController {

    @Autowired
    lateinit var couponService: CouponService

    @Autowired
    lateinit var couponDao: CouponDao

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var managerService: ManagerService

    @RequestMapping("/coupon/list")
    fun couponList(
            modelAndView: ModelAndView,
            @RequestParam(name = "name", required = false, defaultValue = "") name: String?,
            @RequestParam(name = "pageno", required = false, defaultValue = "1") pageno: Int): ModelAndView {

        val loginManager = managerService.loginManager

        val pageable = PageRequest(pageno - 1, 20, Sort.Direction.DESC, "id")
        val coupons = if (name == null || name == "")
            couponDao.findByCompanyId(loginManager!!.companyId, pageable)
        else
            couponDao.findByNameIgnoreCaseAndCompanyId(name, loginManager!!.companyId, pageable)

        modelAndView.model.put("inputCouponName", name)

        val pager = Pager(coupons.totalPages, 7, pageno - 1)
        modelAndView.model.put("pager", pager)

        modelAndView.model.put("coupons", coupons.content)

        modelAndView.model.put("viewCat", "coupon_mgr")
        modelAndView.model.put("viewContent", "coupon_list")
        modelAndView.viewName = "layout"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/coupon/edit"), method = arrayOf(RequestMethod.GET))
    fun editCoupon(
            modelAndView: ModelAndView,
            @RequestParam(name = "id", required = true) id: Int): ModelAndView {

        val loginManager = managerService.loginManager

        var supportedProductTypes: Set<Int> = emptySet<Int>()
        var supportedProductIdSet: Set<Int> = emptySet<Int>()
        var supportedPositionIdSet: Set<Int> = emptySet<Int>()
        var supportedPrintStationIdSet: Set<Int> = emptySet<Int>()

        if (id > 0) {
            val coupon = couponDao.findOne(id)
            supportedProductTypes = coupon.constrains
                    .filter { it.constrainsType == CouponConstrainsType.PRODUCT_TYPE.value }
                    .map { it.value }
                    .toHashSet()
            supportedProductIdSet = coupon.constrains
                    .filter { it.constrainsType == CouponConstrainsType.PRODUCT.value }
                    .map { it.value }
                    .toHashSet()
            supportedPositionIdSet = coupon.constrains
                    .filter { it.constrainsType == CouponConstrainsType.POSITION.value }
                    .map { it.value }
                    .toHashSet()
            supportedPrintStationIdSet = coupon.constrains
                    .filter { it.constrainsType == CouponConstrainsType.PRINT_STATION.value }
                    .map { it.value }
                    .toHashSet()
        }

        val allProductTypes = ProductType.values()
                .map { ProductTypeItem(it.value, it.dispName, supportedProductTypes.contains(it.value)) }
        val products = productService.queryProducts(loginManager!!.companyId, "", true, "sequence asc")
                .map { ProductItem(it.id, it.template.type ,it.name, it.template.name, supportedProductIdSet.contains(it.id)) }
        val allPositions = positionDao.findByCompanyId(loginManager!!.companyId)
                .map { PositionItem(it.id, it.name, it.address, supportedPositionIdSet.contains(it.id)) }
        val allPrintStations = printStationDao.findByCompanyId(loginManager.companyId)
                .map { PrintStationItem(it.id, "自助机${it.id}", it.position.name, supportedPrintStationIdSet.contains(it.id)) }

        var coupon: Coupon? = null
        if (id > 0) {
            coupon = couponDao.findOne(id)
        }

        if (coupon == null) {
            val now = LocalDate.now()
            coupon = Coupon()
            coupon.claimMethod = CouponClaimMethod.INPUT_CODE.value
            coupon.begin = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant())
            coupon.expire = Date.from(now.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant())
            coupon.minExpense = 100
            coupon.discount = 10
            coupon.enabled = true
        }

        modelAndView.model["coupons"] = couponDao.findAll()
        modelAndView.model["claimMethods"] = CouponClaimMethod.values()
        modelAndView.model["productTypes"] = allProductTypes
        modelAndView.model["photo_products"] = products.filter { it.productType == ProductType.PHOTO.value }
        modelAndView.model["template_products"] = products.filter { it.productType == ProductType.TEMPLATE.value }
        modelAndView.model["album_products"] = products.filter { it.productType == ProductType.ALBUM.value }
        modelAndView.model["diy_products"] = products.filter { it.productType == ProductType.DIY.value }
        modelAndView.model["id_photo_products"] = products.filter { it.productType == ProductType.ID_PHOTO.value }
        modelAndView.model["positions"] = allPositions
        modelAndView.model["printStations"] = allPrintStations
        modelAndView.model["productIds"] = products.map { it.productId }.joinToString(separator = ",")
        modelAndView.model["positionIds"] = allPositions.map { it.positionId }.joinToString(separator = ",")
        modelAndView.model["printStationIds"] = allPrintStations.map { it.printStationId }.joinToString(separator = ",")

        if (id > 0) {
            modelAndView.model.put("userRegDays", coupon.constrains
                    .find { it.constrainsType == CouponConstrainsType.USER_REG_DAYS.value }?.value ?: 0)
        }
        else {
            modelAndView.model["userRegDays"] = 0
        }

        modelAndView.model["create"] = id <= 0
        modelAndView.model["coupon"] = coupon
        modelAndView.viewName = "/coupon/edit :: content"

        return modelAndView
    }

    @RequestMapping(path = arrayOf("/coupon/edit"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    fun editCoupon(request: HttpServletRequest,
            @RequestParam(name = "id", required = true) id: Int,
            @RequestParam(name = "name", required = true) name: String,
            @RequestParam(name = "code", required = true) code: String,
            @RequestParam(name = "disabled", required = false) disabled: Boolean?,
            @RequestParam(name = "claimMethod", required = true) claimMethod: Int,
            @RequestParam(name = "maxUses", required = true) maxUses: Int,
            @RequestParam(name = "maxUsesPerUser", required = true) maxUsesPerUser: Int,
            @RequestParam(name = "minExpense", required = true) minExpense: Double,
            @RequestParam(name = "discount", required = true) discount: Double,
            @RequestParam(name = "begin", required = true) begin: String,
            @RequestParam(name = "expire", required = true) expire: String,
            @RequestParam(name = "userRegDays", required = true) userRegDays: Int,
            @RequestParam(name = "productIds", required = true) productIds: String,
            @RequestParam(name = "positionIds", required = true) positionIds: String,
            @RequestParam(name = "printStationIds", required = true) printStationIds: String
    ): CommonRequestResult {
        if (discount > minExpense) {
            throw IllegalArgumentException("折扣金额不能大于最低消费")
        }

        val couponCode = if (code.isEmpty() && claimMethod == 2) getRandomStr(8) else code


        val enabled = !(disabled != null && disabled)

        val selectedProductTypes = ProductType.values()
                .filter { !request.getParameter("productType_${it.value}").isNullOrBlank() }
                .toSet()
        val selectedProductIds = productIds
                .split(',')
                .filter { !request.getParameter("product_$it").isNullOrBlank() }
                .map { it.toInt() }
                .toSet()
        val selectedPositionIds = positionIds
                .split(',')
                .filter { !request.getParameter("position_$it").isNullOrBlank() }
                .map { it.toInt() }
                .toSet()
        val selectedPrintStationIds = printStationIds
                .split(',')
                .filter { !request.getParameter("printStation_$it").isNullOrBlank() }
                .map { it.toInt() }
                .toSet()

        val df = SimpleDateFormat("yyyy-MM-dd")
        val couponClaimMethod = CouponClaimMethod.values().find{ it.value == claimMethod }
        if (id <= 0) {
            couponDao.findFirstByCodeIgnoreCaseOrderByBeginDesc(couponCode)?.let {
                return CommonRequestResult(12138,"已有该代码的优惠券")
            }
            couponService.createCoupon(name, couponCode, enabled, couponClaimMethod!!, maxUses, maxUsesPerUser,
                    (minExpense * 100).toInt(), (discount * 100).toInt(),
                    df.parse(begin), df.parse(expire), userRegDays,
                    selectedProductTypes, selectedProductIds, selectedPositionIds, selectedPrintStationIds)
        } else {
            couponService.updateCoupon(id, name, couponCode, enabled, couponClaimMethod!!, maxUses, maxUsesPerUser,
                    (minExpense * 100).toInt(), (discount * 100).toInt(),
                    df.parse(begin), df.parse(expire), userRegDays,
                    selectedProductTypes, selectedProductIds, selectedPositionIds, selectedPrintStationIds)
        }
        return CommonRequestResult()
    }
}

fun getRandomStr(outputStrLength: Long): String {
    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    return Random().ints(outputStrLength, 0, source.length).toArray()
            .asSequence().map(source::get).joinToString("")
}
package com.unicolour.joyspace.controller.api

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.model.PrintStation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody


@Controller
class PrintStationController {
    @Autowired
    var printStationDao: PrintStationDao? = null

    @RequestMapping("/api/printStation/findByQrCode", method = arrayOf(RequestMethod.GET))
    @ResponseBody
    fun findByQrCode(@RequestParam("qrCode") qrCode: String) : ResponseEntity<PrintStationDTO> {
        val printStation: PrintStation? = printStationDao!!.findByWxQrCode(qrCode);

        if (printStation == null) {
            return ResponseEntity.notFound().build()
        }
        else {
            val ps = PrintStationDTO()

            ps.sn = printStation.sn
            ps.address = printStation.address
            ps.wxQrCode = printStation.wxQrCode
            ps.latitude = printStation.latitude
            ps.longitude = printStation.longitude

            return ResponseEntity.ok(ps)
        }
    }
}

class PrintStationDTO(
        /** 编号 */
        var sn: String = "",
        /** 地址 */
        var address: String = "",
        /** 微信二维码 */
        var wxQrCode: String = "",
        /** 经度 */
        var longitude: Double = 0.0,
        /** 纬度 */
        var latitude: Double = 0.0
)



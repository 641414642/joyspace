package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PrintStationActivationCodeDao
import com.unicolour.joyspace.model.PrintStationActivationCode
import com.unicolour.joyspace.service.PrintStationActivationCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*
import javax.transaction.Transactional

@Component
open class PrintStationActivationCodeServiceImpl : PrintStationActivationCodeService {
    @Autowired
    lateinit var printStationActivationCodeDao: PrintStationActivationCodeDao

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Transactional
    override fun batchCreateActivationCodes(printStationIdStart: Int, quantity: Int,
                                            printerType: String, proportion: Int, adSetId: Int): Boolean {
        val existCodes = HashSet<String>()
        val codes = ArrayList<PrintStationActivationCode>()
        for (i in 0 until quantity) {
            val code = PrintStationActivationCode()
            code.adSetId = if (adSetId == 0) null else adSetId
            code.code = generateRandomCode(existCodes)
            code.createTime = Calendar.getInstance()
            code.printStationId = printStationIdStart + i
            code.printerType = printerType
            code.transferProportion = proportion
            code.useTime = null
            code.used = false

            codes += code
            existCodes += code.code
        }

        printStationActivationCodeDao.save(codes)
        return true
    }

    private fun generateRandomCode(existCodes: HashSet<String>): String {
        var code:String
        do {
            code = BigInteger(8 * 8, secureRandom).toString(36).toUpperCase().substring(0, 8)
        } while (printStationActivationCodeDao.existsByCode(code) || existCodes.contains(code))

        return code
    }
}
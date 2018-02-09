package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.AdSetDao
import com.unicolour.joyspace.dao.PrintStationActivationCodeDao
import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dto.ResultCode
import com.unicolour.joyspace.exception.ProcessException
import com.unicolour.joyspace.model.AdSet
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
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Autowired
    lateinit var secureRandom: SecureRandom

    @Transactional
    override fun batchCreateActivationCodes(printStationIdStart: Int, quantity: Int,
                                            printerType: String, proportion: Int, adSetId: Int): Boolean {

        if (printStationDao.idExistsInRange(printStationIdStart, printStationIdStart + quantity - 1)) {
            throw ProcessException(ResultCode.PRINT_STATION_ID_EXISTS)
        }
        else if (printStationActivationCodeDao.printStationIdExistsInRange(printStationIdStart, printStationIdStart + quantity - 1)) {
            throw ProcessException(ResultCode.PRINT_STATION_CODE_ID_EXISTS)
        }

        var adSet: AdSet? = null
        if (adSetId > 0) {
            adSet = adSetDao.findOne(adSetId)
        }

        val existCodes = HashSet<String>()
        val codes = ArrayList<PrintStationActivationCode>()
        for (i in 0 until quantity) {
            val code = PrintStationActivationCode()
            code.adSetId = adSet?.id
            code.adSetName = adSet?.name
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

    @Transactional
    override fun updateActivationCode(id: Int, printerType: String, proportion: Int, adSetId: Int): Boolean {
        val code = printStationActivationCodeDao.findOne(id)

        var adSet: AdSet? = null
        if (adSetId > 0) {
            adSet = adSetDao.findOne(adSetId)
        }

        code.adSetId = adSet?.id
        code.adSetName = adSet?.name
        code.printerType = printerType
        code.transferProportion = proportion

        printStationActivationCodeDao.save(code)
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
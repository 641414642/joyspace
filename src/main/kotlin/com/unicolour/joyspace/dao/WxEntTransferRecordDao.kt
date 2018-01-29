package com.unicolour.joyspace.dao

import com.unicolour.joyspace.model.WxEntTransferRecord
import com.unicolour.joyspace.model.WxEntTransferRecordItem
import org.springframework.data.repository.CrudRepository
import java.util.*

interface WxEntTransferRecordDao : CrudRepository<WxEntTransferRecord, Int> {
    fun existsByTradeNo(tradeNo: String): Boolean
    fun countByReceiverOpenIdAndTransferTimeAfter(openId:String, time: Calendar) : Long
}

interface WxEntTransferRecordItemDao : CrudRepository<WxEntTransferRecordItem, Int> {
    fun findByPrintOrderId(printOrderId: Int) : WxEntTransferRecordItem?
}

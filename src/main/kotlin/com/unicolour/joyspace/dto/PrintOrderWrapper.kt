package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.*

class PrintOrderWrapper(
        val order: PrintOrder,
        val company: Company?,
        val position: Position,
        val userId: Int,
        val userName: String,
        val transferRecord: WxEntTransferRecord?,
        val transferRecordItem: WxEntTransferRecordItem?
)

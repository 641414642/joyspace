package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.WxEntTransferRecord
import com.unicolour.joyspace.model.WxEntTransferRecordItem

class PrintOrderWrapper(
        val order: PrintOrder,
        val position: Position,
        val userName: String,
        val transferRecord: WxEntTransferRecord?,
        val transferRecordItem: WxEntTransferRecordItem?
)

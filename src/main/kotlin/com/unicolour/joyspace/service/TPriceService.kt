package com.unicolour.joyspace.service

import com.unicolour.joyspace.model.TPriceItem
import java.util.*

interface TPriceService {

    fun createtp(name: String,begin: Date, expire: Date, product_id: Int,position: Int,tpriceItem: List<TPriceItem>): Boolean

    fun updatetp(id: Int,name: String,begin: Date, expire: Date, product_id: Int,position: Int,tpriceItem: List<TPriceItem>): Boolean

    fun tpriceEnabled(id: Int): Boolean


    fun updatetpItem(item_id: Int,item: TPriceItem): Boolean

}


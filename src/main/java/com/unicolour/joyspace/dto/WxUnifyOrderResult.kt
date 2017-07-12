package com.unicolour.joyspace.dto

import javax.xml.bind.annotation.XmlAnyElement
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "xml")
class WxUnifyOrderResult {
    @set:XmlElement
    var return_code: String? = null

    @set:XmlElement
    var return_msg: String? = null

    @set:XmlElement
    var appid: String? = null

    @set:XmlElement
    var mch_id: String? = null

    @set:XmlElement
    var nonce_str: String? = null

    @set:XmlElement
    var sign: String? = null

    @set:XmlElement
    var result_code: String? = null

    @set:XmlElement
    var prepay_id: String? = null

    @set:XmlElement
    var trade_type: String? = null

    @set:XmlAnyElement(lax = true)
    var other: List<Any>? = null
}
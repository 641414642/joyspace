package com.unicolour.joyspace.dto

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "xml")
class WxUnifyOrderResult {
    @set:XmlElement
    var return_code: String = ""

    @set:XmlElement
    var return_msg: String = ""

    @set:XmlElement
    var appid: String = ""

    @set:XmlElement
    var mch_id: String = ""

    @set:XmlElement
    var nonce_str: String = ""

    @set:XmlElement
    var sign: String = ""

    @set:XmlElement
    var result_code: String = ""

    @set:XmlElement
    var prepay_id: String = ""

    @set:XmlElement
    var trade_type: String = ""
}
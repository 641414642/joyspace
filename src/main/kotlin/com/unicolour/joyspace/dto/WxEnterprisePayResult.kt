package com.unicolour.joyspace.dto

import javax.xml.bind.annotation.XmlAnyElement
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "xml")
class WxEnterprisePayResult {
    @set:XmlElement
    var return_code: String? = null  //String(16) 	SUCCESS/FAIL  此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断

    @set:XmlElement
    var return_msg: String? = null  //	String(128) 	返回信息，如非空，为错误原因

//以下字段在return_code为SUCCESS的时候有返回

    @set:XmlElement
    var mch_appid: String? = null  //	String 	微信分配的公众账号ID（企业号corpid即为此appId）

    @set:XmlElement
    var mchid: String? = null  //	String(32) 	微信支付分配的商户号

    @set:XmlElement
    var device_info: String? = null  //	String(32) 	微信支付分配的终端设备号，

    @set:XmlElement
    var nonce_str: String? = null  // String(32) 	随机字符串，不长于32位

    @set:XmlElement
    var result_code: String? = null //	String(16) 	SUCCESS/FAIL

    @set:XmlElement
    var err_code: String? = null  //	String(32) 	错误码信息

    @set:XmlElement
    var err_code_des: String? = null  //	String(128) 	结果信息描述

//以下字段在return_code 和result_code都为SUCCESS的时候有返回

    @set:XmlElement
    var partner_trade_no: String? = null   // String(32) 	商户订单号，需保持唯一性  只能是字母或者数字，不能包含有符号

    @set:XmlElement
    var payment_no: String? = null //	String 	企业付款成功，返回的微信订单号

    @set:XmlElement
    var payment_time: String? = null   //  String 	企业付款成功时间

    @set:XmlAnyElement(lax = true)
    var other: List<Any>? = null
}

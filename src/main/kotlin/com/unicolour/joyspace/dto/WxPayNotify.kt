package com.unicolour.joyspace.dto

import javax.xml.bind.annotation.XmlAnyElement
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "xml")
class WxPayNotify {
    @set:XmlElement
    var return_code: String? = null

    @set:XmlElement
    var appid: String? = null //	是	String(32)	wx8888888888888888	微信分配的小程序ID


    @set:XmlElement
    var mch_id: String? = null //	是	String(32)	1900000109	微信支付分配的商户号

    @set:XmlElement
    var device_info: String? = null //	否	String(32)	013467007045764	微信支付分配的终端设备号，

    @set:XmlElement
    var nonce_str: String? = null //	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位

    @set:XmlElement
    var sign: String? = null //	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	签名，详见签名算法

    @set:XmlElement
    var sign_type: String? = null //	否	String(32)	HMAC-SHA256	签名类型，目前支持HMAC-SHA256和MD5，默认为MD5

    @set:XmlElement
    var result_code: String? = null //	是	String(16)	SUCCESS	SUCCESS/FAIL

    @set:XmlElement
    var err_code: String? = null //	否	String(32)	SYSTEMERROR	错误返回的信息描述

    @set:XmlElement
    var err_code_des: String? = null //	否	String(128)	系统错误	错误返回的信息描述

    @set:XmlElement
    var openid: String? = null //	是	String(128)	wxd930ea5d5a258f4f	用户在商户appid下的唯一标识

    @set:XmlElement
    var is_subscribe: String? = null //	否	String(1)	Y	用户是否关注公众账号，Y-关注，N-未关注，仅在公众账号类型支付有效

    @set:XmlElement
    var trade_type: String? = null // //	是	String(16)	JSAPI	JSAPI、NATIVE、APP

    @set:XmlElement
    var bank_type: String? = null //	是	String(16)	CMC	银行类型，采用字符串类型的银行标识，银行类型见银行列表

    @set:XmlElement
    var total_fee: Int? = 0 //	是	Int	100	订单总金额，单位为分

    @set:XmlElement
    var settlement_total_fee: Int? = 0 //	否	Int	100	应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。

    @set:XmlElement
    var fee_type: String? = null //	否	String(8)	CNY	货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型

    @set:XmlElement
    var cash_fee: Int? = 0 //	是	Int	100	现金支付金额订单现金支付金额，详见支付金额

    @set:XmlElement
    var cash_fee_type: String? = null //	否	String(16)	CNY	货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型

    @set:XmlElement
    var coupon_fee: Int? = 0 //	否	Int	10	代金券金额<=订单金额，订单金额-代金券金额=现金支付金额，详见支付金额

    @set:XmlElement
    var coupon_count: Int? = 0 //	否	Int	1	代金券使用数量

//    代金券类型 coupon_type_$n	否	Int	CASH
//    代金券ID	coupon_id_$n	否	String(20)	10000	代金券ID,$n为下标，从0开始编号
//    单个代金券支付金额	coupon_fee_$n	否	Int	100	单个代金券支付金额,$n为下标，从0开始编号

    @set:XmlElement
    var transaction_id: String? = null //	是	String(32)	1217752501201407033233368018	微信支付订单号

    @set:XmlElement
    var out_trade_no: String? = null //	是	String(32)	1212321211201407033568112322	商户系统的订单号，与请求一致。

    @set:XmlElement
    var attach: String? = null //	否	String(128)	123456	商家数据包，原样返回

    @set:XmlElement
    var time_end: String? = null //	是	String(14)	20141030133525	支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则

    @set:XmlAnyElement(lax = true)
    var other: List<Any>? = null
}
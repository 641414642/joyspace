package com.unicolour.joyspace.dto

data class WxPayParams(
    var timeStamp: String, //时间戳从1970年1月1日00:00:00至今的秒数,即当前的时间
    var nonceStr: String, //随机字符串，长度为32个字符以下。
    var pkg: String, //统一下单接口返回的 prepay_id 参数值，提交格式如：prepay_id=*
    var paySign: String  //签名,具体签名方案参见微信公众号支付帮助文档;
)
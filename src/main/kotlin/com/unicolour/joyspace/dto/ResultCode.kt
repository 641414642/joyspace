package com.unicolour.joyspace.dto

enum class ResultCode(val value:Int, val desc:String, val descEn: String) {
    SUCCESS(0, "成功", "Success"),
    PHONE_NUMBER_ALREADY_REGISTERED(1, "手机号码已经注册过", "Phone number already registered"),
    NICK_NAME_ALREADY_REGISTERED(2, "用户昵称已经注册过", "Nick name already registered"),
    INVALID_VERIFY_CODE(3, "无效或过期的验证码", "Invalid verify code"),
    RETRY_LATER(4, "请求太频繁(请等待60秒以后再试)", "Retry later"),
    SEND_VERIFY_CODE_FAILED(5, "发送验证码失败", "Send verify code failed"),
    PRINT_ORDER_NOT_FOUND(6, "没有找到指定的订单", "Print order not found"),
    USER_NOT_FOUND_FOR_THIS_PHONE_NUMBER(7, "此手机号码没有对应的注册用户", "No user found for this phone number"),
    USER_NOT_FOUND(8, "没有找到指定的用户", "User not found"),
    PRINT_STATION_NOT_FOUND(9, "没有找到自助机", "PrintStation not found"),
    CITY_NOT_FOUND(10, "没有找到指定位置所属的城市", "City not found for specified location"),
    INVALID_PRINT_STATION_LOGIN_SESSION(11, "自助机没有登录或登录超时", "PrintStation not login or session timeout"),
    NOT_IN_THIS_PRINT_STATION(12, "不是此自助机的订单", "Print order not in this print station"),
    INVALID_USER_LOGIN_SESSION(13, "用户没有登录", "User not login"),
    INVALID_ACTIVATION_CODE(14, "无效的激活码", "Invalid activation code"),
    ACTIVATION_CODE_USED(15, "激活码已经使用过", "Activation code has bean used"),
    GEO_DECODE_FAILED(16, "查找地址信息失败", "Failed to get address info of location"),
    PRINT_STATION_ID_EXISTS(17, "自助机ID和现有的自助机重复", ""),
    PRINT_STATION_CODE_ID_EXISTS(18, "自助机ID和现有的自助机激活码重复", ""),
    COMPANY_WX_ACCOUNT_EXISTS(19, "此微信帐户已经添加过", ""),
    COMPANY_ALREADY_EXISTS(20, "同名投放商已存在", "Company with same name already exists"),
    MANAGER_ALREADY_EXISTS(21, "同名管理员已存在", "Manager with same name already exists"),
    COMPANY_WX_ACCOUNT_NOT_EXISTS(22, "指定的帐户不存在", ""),
    EXCEED_MAX_WX_ACCOUNT_NUMBER(23, "最多只能添加10个微信收款账户", ""),

    SERVER_ERROR(100, "服务器错误", "Server error"),

    OTHER_ERROR(200, "其他错误", "Other error"),
}
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

    COMPANY_ALREADY_EXISTS(20, "同名投放商已存在", "Company with same name already exists"),
    MANAGER_ALREADY_EXISTS(21, "同名管理员已存在", "Manager with same name already exists"),
    SERVER_ERROR(100, "服务器错误", "Server error"),

    OTHER_ERROR(200, "其他错误", "Other error"),
}
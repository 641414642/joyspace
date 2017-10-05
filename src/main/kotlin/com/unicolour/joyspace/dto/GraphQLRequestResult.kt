package com.unicolour.joyspace.dto

class GraphQLRequestResult(var resultCode: ResultCode) {
    fun getResult() : Int {
        return resultCode.value
    }

    fun getDescription() : String {
        return resultCode.desc
    }
}

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
    SERVER_ERROR(100, "服务器错误", "Server error"),
}
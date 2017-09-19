package com.unicolour.joyspace.dto

import com.unicolour.joyspace.model.User

//登录请求结果
class AppUserLoginResult(
        //结果代码 0: 成功,  1: 手机号或密码错误, 2: 用户名或密码错误,  3: 缺少用户名或手机号
        var result: Int = 0,

        //结果的描述
        var description: String? = null,

        var session: AppUserLoginSession? = null
)

class AppUserLoginSession(
        //登录sessionId
        var sessionId: String,

        //登录用户信息
        var userInfo: User
)
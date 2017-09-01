package com.unicolour.joyspace.dto

class UserDTO(
        var userName: String? = null,
        var wxOpenId: String? = null,
        var fullName: String? = null,
        var sex: String = "M",     //M 男   F 女
        var email: String? = null,
        var phone: String? = null,
        var enabled: Boolean = true
)
package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/** 用户登录会话 */
@Entity
@Table(name = "user_login_session")
class UserLoginSession {
    @Id
    @Column(length = 32)
    var id: String = ""

    //微信session_key
    @Column(length = 50)
    var wxSessionKey: String = ""

    //微信openId
    @Column(length = 50)
    var wxOpenId: String = ""

    //用户Id
    @Column
    var userId: Int = 0

    //过期时间
    @Column
    lateinit var expireTime: Calendar
}

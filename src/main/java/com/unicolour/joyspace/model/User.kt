package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 最终用户 */
@Entity
@Table(name = "user")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    /** 用户 */
    @NotNull
    @Column(length = 200)
    var userName: String? = null

    /** 微信OpenID */
    @NotNull
    @Column(length = 200)
    var wxOpenId: String? = null

    /** 真实姓名 */
    @Column(length = 80)
    var fullName: String? = null

    @Column
    var sex: Byte = 0     //1 男   2 女   其他值 未知

    @Column(length = 80)
    var email: String? = null

    @Column(length = 50)
    var phone: String? = null

    @NotNull
    lateinit var createTime: Calendar

    @NotNull
    var enabled: Boolean = false
}
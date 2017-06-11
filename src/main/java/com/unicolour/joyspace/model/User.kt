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
    private var userName: String? = null

    /** 微信OpenID */
    @NotNull
    @Column(length = 200)
    private var wxOpenId: String? = null

    /** 真实姓名 */
    @Column(length = 80)
    private var fullName: String? = null

    @Column
    private var sex: Byte = 0     //0 男   1 女

    @Column(length = 80)
    private var email: String? = null

    @Column(length = 50)
    private var phone: String? = null

    @NotNull
    private var createTime: Calendar? = null

    @NotNull
    private var enabled: Boolean = false
}
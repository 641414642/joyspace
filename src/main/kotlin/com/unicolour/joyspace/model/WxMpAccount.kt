package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * 微信公众号信息
 */
@Entity
@Table(name = "wx_mp_account")
class WxMpAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 二维码 */
    @Column(length = 255)
    @NotNull
    var qrCode: String = ""

    @Column(length = 100)
    @NotNull
    var appId: String = ""

    @Column(length = 100)
    @NotNull
    var wxAppSecret: String = ""

    @NotNull
    var active: Boolean = false
}
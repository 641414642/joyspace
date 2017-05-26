package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 微信支付参数  */
@Entity
@Table(name = "wei_xin_pay_config")
class WeiXinPayConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String? = null

    @Column
    var enabled: Boolean = false

    @Column(length = 50)
    @NotNull
    var appId: String? = null

    @Column(length = 50)
    @NotNull
    var mchId: String? = null

    @Column(length = 50)
    @NotNull
    var keyVal: String? = null

    @Column(length = 50)
    @NotNull
    var appSecret: String? = null
}

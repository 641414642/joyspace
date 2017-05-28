package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 支付宝支付参数  */
@Entity
@Table(name = "ali_pay_config")
class AliPayConfig {
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
    var sellerEmail: String? = null

    @Column(length = 50)
    @NotNull
    var partner: String? = null

    @Column(length = 50)
    @NotNull
    var keyVal: String? = null

    @Column(length = 50)
    @NotNull
    var inputCharset: String? = null

    @Column(length = 50)
    @NotNull
    var signType: String? = null
}
package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "company")
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    @NotNull
    @Column
    lateinit var createTime: Calendar

    //region 缺省价目表列
    /** 价目表ID */
    @Column(name = "default_price_list_id", insertable = false, updatable = false)
    var defaultPriceListId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_price_list_id")
    var defaultPriceList: PriceList? = null
    //endregion

    //region 微信支付参数
    /** 微信支付参数ID */
    @Column(name = "wei_xin_pay_config_id", insertable = false, updatable = false)
    var weiXinPayConfigId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wei_xin_pay_config_id")
    var weiXinPayConfig: WeiXinPayConfig? = null
    //endregion
}

//收款帐户信息
@Entity
@Table(name = "company_wx_account")
class CompanyWxAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    //属于哪个投放商
    @Column
    @NotNull
    var companyId: Int = 0

    //收款openId
    @Column
    @NotNull
    lateinit var openId: String

    //收款人姓名
    @Column(length = 50)
    @NotNull
    lateinit var name: String

    @Column
    @NotNull
    var enabled: Boolean = false   //是否可用
}
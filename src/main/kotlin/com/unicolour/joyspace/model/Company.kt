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

    @Column
    @NotNull
    var businessModel: Int = 0

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
}

//经营方式
enum class BusinessModel(
        val value: Int,
        val displayName: String
) {
    DEFAULT(0, ""),
    INVEST(1, "投放"),
    PURCHASE(2, "购买"),
    PARTNER(3, "合作")
}


//收款帐户信息
@Entity
@Table(name = "company_wx_account")
class CompanyWxAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    //关联的公众号id
    @Column
    @NotNull
    var wxMpAccountId: Int = 0

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

    //收款人微信昵称
    @Column(length = 50)
    @NotNull
    lateinit var nickName: String

    //电话号码
    @Column(length = 20)
    @NotNull
    lateinit var phoneNumber: String

    //头像
    @Column(length = 1024)
    var avatar: String? = null

    //验证码
    @Column(length = 6)
    @NotNull
    lateinit var verifyCode: String

    //创建时间
    @Column
    @NotNull
    lateinit var createTime: Calendar

    @Column
    @NotNull
    var enabled: Boolean = false   //是否可用

    @Column
    @NotNull
    var sequence: Int = 0
}
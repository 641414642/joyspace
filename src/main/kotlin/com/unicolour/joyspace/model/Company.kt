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
}
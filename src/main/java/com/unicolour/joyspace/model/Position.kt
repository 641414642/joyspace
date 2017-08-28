package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 投放地点 */
@Entity
@Table(name = "position")
class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 地址 */
    @Column(length = 255)
    @NotNull
    var address: String = ""

    /** 经度 */
    @Column
    @NotNull
    var longitude: Double = 0.0

    /** 纬度 */
    @Column
    @NotNull
    var latitude: Double = 0.0

    //region 店面
    /** 店面ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @NotNull
    lateinit var company: Company
    //endregion

    //region 价目表列
    /** 价目表ID */
    @Column(name = "price_list_id", insertable = false, updatable = false)
    var priceListId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id")
    var priceList: PriceList? = null
    //endregion

    @OneToMany(mappedBy = "position")
    lateinit var printStations: List<PrintStation>
}

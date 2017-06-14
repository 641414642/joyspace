package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 投放地点 */
@Entity
@Table(name = "position")
class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    /** 属于哪个商家 */
    @Column
    @NotNull
    var companyId: Int = 0

    /** 价目表ID */
    @Column
    @NotNull
    var priceListId: Int? = null

    @OneToMany(mappedBy = "position")
    var printStations: List<PrintStation>? = null
}

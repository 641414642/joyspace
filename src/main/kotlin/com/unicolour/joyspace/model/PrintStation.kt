package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.persistence.JoinColumn
import javax.persistence.FetchType



/** 打印自助机 */
@Entity
@Table(name = "print_station")
class PrintStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    /** 微信二维码 */
    @Column(length = 255)
    @NotNull
    var wxQrCode: String = ""

    @Column(name = "position_id", insertable = false, updatable = false)
    var positionId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    lateinit var position: Position

    @Column(length = 128)
    lateinit var password: String

    //region 店面
    /** 店面ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @NotNull
    lateinit var company: Company
    //endregion

    //region 城市
    /** 城市ID */
    @Column(name = "city_id", insertable = false, updatable = false)
    var cityId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    @NotNull
    lateinit var city: City
    //endregion

    @Column(name = "status")
    var status: Int = PrintStationStatus.NORMAL.value
}

enum class PrintStationStatus(val value:Int, val message:String) {
    NORMAL(0, "正常"),
    MALFUNCTION(1, "故障"),
    OUT_OF_PRINTING_SUPPLIES(2, "打印耗材用完")
}


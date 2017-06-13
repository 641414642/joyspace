package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 打印自助机 */
@Entity
@Table(name = "print_station")
class PrintStation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    /** 编号 */
    @Column(length = 50)
    @NotNull
    var sn: String = ""

    /** 地址 */
    @Column(length = 255)
    @NotNull
    var address: String = ""

    /** 微信二维码 */
    @Column(length = 255)
    @NotNull
    var wxQrCode: String = ""

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
}
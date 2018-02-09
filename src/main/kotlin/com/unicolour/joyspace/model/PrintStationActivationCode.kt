package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 自助机激活码 */
@Entity
@Table(name = "print_station_activation_code")
class PrintStationActivationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 20)
    @NotNull
    var code: String = ""

    @Column
    @NotNull
    var printStationId: Int = 0

    @Column(length = 128)
    @NotNull
    lateinit var printerType: String

    /** 广告ID */
    @Column
    var adSetId: Int? = null

    @Column(length = 50)
    var adSetName: String? = null

    //分账比例 x 1000
    @Column(name = "transfer_proportion")
    @NotNull
    var transferProportion: Int = 1000

    @Column
    @NotNull
    var used: Boolean = false

    @Column
    @NotNull
    lateinit var createTime: Calendar

    @Column
    var useTime: Calendar? = null
}
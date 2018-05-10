package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 广告集 */
@Entity
@Table(name = "printer_stat_record")
class PrinterStatRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @NotNull
    @Column
    lateinit var reportTime: Calendar

    @Column
    @NotNull
    var companyId: Int = 0     //属于哪个投放商

    @Column
    @NotNull
    var positionId: Int = 0

    @Column
    @NotNull
    var printStationId: Int = 0

    @Column(length = 100)
    var printerSerialNo: String? = null

    @Column(length = 100)
    var printerType: String? = null

    @Column(length = 100)
    var printerName: String? = null

    @Column
    @NotNull
    var mediaCounter: Int = 0

    @Column(length = 20)
    var sendToPhoneNumber: String? = null
}


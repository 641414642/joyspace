package com.unicolour.joyspace.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

/** 打印机类型 */
@Entity
@Table(name = "printer_type")
class PrinterType {
    /** 名称 */
    @Id
    @Column(length = 50)
    var name: String = ""

    /** 打印分辨率 */
    @Column
    @NotNull
    var resolution: Int = 0

    /** 耗材报警阈值列表 (从高到低，用逗号分隔) */
    @Column
    @NotNull
    var mediaAlertThresholds: String = ""
}


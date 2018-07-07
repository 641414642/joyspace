package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** Icc文件设置 */
@Entity
@Table(name = "icc_config")
class IccConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    /** 打印机型号 */
    @Column(length = 100)
    @NotNull
    var printerModel: String = ""

    /** 操作系统名称, 为空表示不区分操作系统 */
    @Column(length = 50)
    var osName: String? = null

    /** icc文件名 */
    @Column(length = 100)
    @NotNull
    var iccFileName: String = ""
}
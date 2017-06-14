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
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    /** 编号 */
    @Column(length = 50)
    @NotNull
    var sn: String = ""

    /** 微信二维码 */
    @Column(length = 255)
    @NotNull
    var wxQrCode: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    val position: Position? = null
}
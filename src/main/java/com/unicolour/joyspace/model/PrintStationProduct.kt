package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 打印自助机支持的产品 */
@Entity
@Table(name = "print_station_product")
class PrintStationProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @Column
    @NotNull
    var printStationId: Int = 0

    @Column
    @NotNull
    var productId: Int = 0
}
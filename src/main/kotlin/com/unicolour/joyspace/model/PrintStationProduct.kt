package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 打印自助机支持的产品 */
@Entity
@Table(name = "print_station_product")
class PrintStationProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0


    @Column(name = "print_station_id", insertable = false, updatable = false)
    var printStationId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "print_station_id")
    @NotNull
    lateinit var printStation: PrintStation


    @Column(name = "product_id", insertable = false, updatable = false)
    var productId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @NotNull
    lateinit var product: Product
}
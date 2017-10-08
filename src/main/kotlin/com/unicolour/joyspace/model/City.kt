package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 投放城市 */
@Entity
@Table(name = "city")
class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    @Column
    @NotNull
    var minLatitude: Double = 0.0

    @Column
    @NotNull
    var maxLatitude: Double = 0.0

    @Column
    @NotNull
    var minLongitude: Double = 0.0

    @Column
    @NotNull
    var maxLongitude: Double = 0.0
}
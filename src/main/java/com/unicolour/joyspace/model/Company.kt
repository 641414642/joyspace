package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "company")
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 缺省的价目表 */
    @Column
    @NotNull
    var defaultPriceListId: Int? = null
}
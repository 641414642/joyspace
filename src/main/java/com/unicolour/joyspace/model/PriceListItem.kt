package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 价目表项目 */
@Entity
@Table(name = "price_list_item")
class PriceListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    /** 属于哪个价目表 */
    @Column
    @NotNull
    var priceListId: Int = 0

    /** 产品ID */
    @Column
    @NotNull
    var productId: Int = 0

    /** 价格(单位是分) */
    @Column
    @NotNull
    var price: Int = 0
}

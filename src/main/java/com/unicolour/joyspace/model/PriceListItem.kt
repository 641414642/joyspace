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

    /** 价格(单位是分) */
    @Column
    @NotNull
    var price: Int = 0

    /** 属于哪个价目表 */
    @Column(name = "price_list_id", insertable = false, updatable = false)
    var priceListId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id")
    @NotNull
    lateinit var priceList: PriceList

    /** 产品ID */
    @Column(name = "product_id", insertable = false, updatable = false)
    var productId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @NotNull
    lateinit var product: Product
}

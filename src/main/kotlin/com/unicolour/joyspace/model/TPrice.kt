package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 梯度价格 */
@Entity
@Table(name = "t_price")
class TPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    //region 商家
    /** 商家ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: Company? = null
    //endregion

    //region 店面
    /** 店面ID */
    @Column(name = "position_id", insertable = false, updatable = false)
    var positionId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    var position: Position? = null
    //endregion

    @Column
    var begin: Date? = null   //生效日期

    @Column
    var expire: Date? = null  //过期日期

    @Column
    @NotNull
    var enabled: Boolean = false   //是否可用

    /** 产品ID */
    @Column(name = "product_id", insertable = false, updatable = false)
    var productId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @NotNull
    lateinit var product: Product


    @OneToMany(mappedBy = "tPrice")
    @OrderBy("id asc")
    lateinit var tPriceItems: List<TPriceItem>
}


/** 梯度单价区间 */
@Entity
@Table(name = "t_price_item")
class TPriceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    /** 价格(单位是分) */
    @Column
    @NotNull
    var price: Int = 0

    /** 属于哪个梯度 */
    @Column(name = "t_price_id", insertable = false, updatable = false)
    var tPriceId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "t_price_id")
    @NotNull
    lateinit var tPrice: TPrice

    @Column
    @NotNull
    var minCount: Int = 0//最小张数

    @Column
    @NotNull
    var maxCount: Int = 0//最大张数


}
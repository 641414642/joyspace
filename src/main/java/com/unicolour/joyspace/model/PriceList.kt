package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 价目表 */
@Entity
@Table(name = "price_list")
class PriceList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    //region 店面
    /** 店面ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    var company: Company? = null
    //endregion

    @OneToMany(mappedBy = "priceList")
    lateinit var priceListItems: List<PriceListItem>
}

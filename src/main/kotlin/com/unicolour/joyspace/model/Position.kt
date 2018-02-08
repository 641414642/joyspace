package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 投放地点 */
@Entity
@Table(name = "position")
class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 地址 */
    @Column(length = 255)
    @NotNull
    var address: String = ""

    /** 经度 */
    @Column
    @NotNull
    var longitude: Double = 0.0

    /** 纬度 */
    @Column
    @NotNull
    var latitude: Double = 0.0

    @Column(length = 100)
    var addressNation: String? = null

    @Column(length = 100)
    var addressProvince: String? = null

    @Column(length = 100)
    var addressCity: String? = null

    @Column(length = 100)
    var addressDistrict: String? = null

    @Column(length = 100)
    var addressStreet: String? = null

    /** 交通情况 */
    @Column(length = 1000)
    var transportation: String = ""

    //region 店面
    /** 店面ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @NotNull
    lateinit var company: Company
    //endregion

    //region 价目表列
    /** 价目表ID */
    @Column(name = "price_list_id", insertable = false, updatable = false)
    var priceListId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id")
    var priceList: PriceList? = null
    //endregion

    @OneToMany(mappedBy = "position")
    lateinit var printStations: List<PrintStation>

    @OneToMany(mappedBy = "position")
    lateinit var imageFiles: List<PositionImageFile>
}


/** 投放地点图片 */
@Entity
@Table(name = "position_image_file")
class PositionImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    /** 文件类型 jpg, png ... */
    @NotNull
    @Column(length = 10)
    lateinit var fileType: String

    /** 属于哪个投放地点 */
    @Column(name = "position_id", insertable = false, updatable = false)
    var positionId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @NotNull
    lateinit var position: Position
}

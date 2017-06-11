package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 产品 */
@Entity
@Table(name = "product")
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 类型 */
    @Column
    @NotNull
    var type: Int = ProductType.PHOTO.value;

    /** 产品编号 */
    @Column(length = 50)
    @NotNull
    var sn: String = ""

    /** 产品分辨率X */
    @Column
    @NotNull
    var resolutionX: Int = 0;

    /** 产品分辨率Y */
    @Column
    @NotNull
    var resolutionY: Int = 0;

    /** 最小图片数量 */
    @Column
    @NotNull
    var minImageCount: Int = 0;

    @Column
    @NotNull
    var enabled: Boolean = false

    /** 备注 */
    @Column(length = 255)
    var remark: String? = null

    /** 缺省价格(单位是分) */
    @Column
    @NotNull
    var defaultPrice: Int = 0
}

/** 产品类别 */
enum class ProductType(val value:Int, val dispName:String) {
    PHOTO(0, "照片输出")
}

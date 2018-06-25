package com.unicolour.joyspace.model

import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotNull


/** 产品模板 */
@Entity
@Table(name = "template")
class Template : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 类型 */
    @Column
    @NotNull
    var type: Int = ProductType.PHOTO.value

    /** 产品宽度(mm) */
    @Column
    @NotNull
    var width: Double = 0.0

    /** 产品高度(mm) */
    @Column
    @NotNull
    var height: Double = 0.0

    /** 最小图片数量 */
    @Column
    @NotNull
    var minImageCount: Int = 0

    /** 当前版本号 */
    @Column
    @NotNull
    var currentVersion: Int = 1

    /** UUID */
    @Column(length = 50)
    @NotNull
    var uuid: String = ""

    @Column(length = 2000)
    var tplParam: String? = ""

    @Column
    @NotNull
    var deleted: Boolean = false

//    //region 投放商
//    /** 投放商ID */
//    @Column(name = "company_id", insertable = false, updatable = false)
//    var companyId: Int = 0
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "company_id")
//    @NotNull
//    lateinit var company: Company
//    //endregion
//
//    /** 是否是公用模板 */
//    @Column
//    @NotNull
//    var publicTemplate: Boolean = false
}

/** 产品模板中的图片信息 */
@Entity
@Table(name = "template_image_info")
class TemplateImageInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    /** 模板id */
    @Column
    var templateId: Int = 0

    /** 模板版本号 */
    @Column
    var templateVersion: Int = 0

    /** 是否是用户需要上传的图片 */
    @Column
    var userImage: Boolean = false

    /**
     * 图片所属层类型
     */
    @Column
    var layerType: Int = LayerType.IMAGE.value

    /**
     * 类型
     */
    @Column
    var type: Int = TemplateImageType.USER.value

    @Column(length = 255)
    var href: String? = null

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 图片框x(mm) */
    @Column
    @NotNull
    var x: Double = 0.0

    /** 图片框y(mm) */
    @Column
    @NotNull
    var y: Double = 0.0

    /** 图片框宽度(mm) */
    @Column
    @NotNull
    var width: Double = 0.0

    /** 图片框高度(mm) */
    @Column
    @NotNull
    var height: Double = 0.0

    /** 旋转角度 */
    @Column
    @NotNull
    var angleClip: Double = 0.0
}

/** 层类型 */
enum class LayerType(val value: Int, val dispName: String) {
    FRONT(0, "前景层"),
    BACKGROUND(1, "背景层"),
    IMAGE(2, "图像层"),
    CONTROL(3, "控制层")
}

/** 层类型 */
enum class TemplateImageType(val value: Int, val dispName: String) {
    USER(0, "用户填图"),
    STICKER(1, " 模板贴图"),
    COLOR(2, "色块"),
}


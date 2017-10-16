package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 产品模板 */
@Entity
@Table(name = "template")
class Template {
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

    @OneToMany(mappedBy = "template")
    lateinit var images: List<TemplateImageInfo>
}

/** 产品模板中的图片信息 */
@Entity
@Table(name = "template_image_info")
class TemplateImageInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 是否是用户需要上传的图片 */
    @Column
    var userImage: Boolean = false

    @Column(length = 255)
    var href: String? = null

    /** 图片框x(mm) */
    @Column
    @NotNull
    var x: Double = 0.0

    /** 图片框y(mm) */
    @Column
    @NotNull
    var y: Double = 0.0

    /** 图片宽度(mm) */
    @Column
    @NotNull
    var wid: Double = 0.0

    /** 图片高度(mm) */
    @Column
    @NotNull
    var hei: Double = 0.0

    /** 变换后的图片框x(mm) */
    @Column
    @NotNull
    var tx: Double = 0.0

    /** 变换后的图片框y(mm) */
    @Column
    @NotNull
    var ty: Double = 0.0

    /** 变换后的图片宽度(mm) */
    @Column
    @NotNull
    var tw: Double = 0.0

    /** 变换后的图片高度(mm) */
    @Column
    @NotNull
    var th: Double = 0.0

    /** 图形变换矩阵 */
    @Column(length = 200)
    @NotNull
    var matrix: String = ""

    /** 属于哪个模板 */
    @Column(name = "template_id", insertable = false, updatable = false)
    var templateId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @NotNull
    lateinit var template: Template
}

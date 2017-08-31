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
    var type: Int = ProductType.PHOTO.value;

    /** 产品宽度(mm) */
    @Column
    @NotNull
    var width: Double = 0.0;

    /** 产品高度(mm) */
    @Column
    @NotNull
    var height: Double = 0.0;

    /** 最小图片数量 */
    @Column
    @NotNull
    var minImageCount: Int = 0;

    /** 当前版本号 */
    @Column
    @NotNull
    var currentVersion: Int = 1

    /** UUID */
    @Column(length = 50)
    @NotNull
    var uuid: String = ""
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

    /** 图片宽度(mm) */
    @Column
    @NotNull
    var width: Double = 0.0;

    /** 图片高度(mm) */
    @Column
    @NotNull
    var height: Double = 0.0;

    /** 属于哪个模板 */
    @Column(name = "template_id", insertable = false, updatable = false)
    var templateId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @NotNull
    lateinit var template: Template
}

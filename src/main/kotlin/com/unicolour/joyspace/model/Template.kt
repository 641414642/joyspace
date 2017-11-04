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

    @OneToMany(mappedBy = "template")
    @OrderBy("id ASC")
    lateinit var images: List<TemplateImageInfo>
}

/** 产品模板中的图片信息 */
@Entity
@Table(name = "template_image_info")
class TemplateImageInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            JoinColumn(name = "templateVersion", referencedColumnName = "currentVersion"),
            JoinColumn(name = "templateId", referencedColumnName = "id")
    )
    lateinit var template: Template

    /** 模板id */
    @Column(insertable = false, updatable = false)
    var templateId: Int = 0

    /** 模板版本号 */
    @Column(insertable = false, updatable = false)
    var templateVersion: Int = 0

    /** 是否是用户需要上传的图片 */
    @Column
    var userImage: Boolean = false

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
}

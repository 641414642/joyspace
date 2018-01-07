package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 广告集 */
@Entity
@Table(name = "ad_set")
class AdSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    lateinit var name: String

    @Column
    @NotNull
    var companyId: Int = 0   //属于哪个投放商

    @NotNull
    @Column
    lateinit var createTime: Calendar

    @NotNull
    @Column
    lateinit var updateTime: Calendar

    @OneToMany(mappedBy = "adSet")
    lateinit var imageFiles: List<AdImageFile>
}

/** 广告图片 */
@Entity
@Table(name = "ad_image_file")
class AdImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @NotNull
    @Column(length = 50)
    lateinit var fileName: String

    /** 文件类型 jpg, png ... */
    @NotNull
    @Column(length = 10)
    lateinit var fileType: String

    @Column
    @NotNull
    var width: Int = 0

    @Column
    @NotNull
    var height: Int = 0

    @NotNull
    @Column(length = 100)
    lateinit var description: String

    /** 播放持续时间（秒） */
    @NotNull
    @Column(length = 100)
    var duration: Int = 5

    @Column
    @NotNull
    var sequence: Int = 0

    /** 属于哪个广告集 */
    @Column(name = "ad_set_id", insertable = false, updatable = false)
    var adSetId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_set_id")
    @NotNull
    lateinit var adSet: AdSet
}


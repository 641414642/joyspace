package com.unicolour.joyspace.model

import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotNull

///** 相册 */
//@Entity
//@Table(name = "album")
//class Album : Serializable {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    var id: Int = 0
//
//    @Column(length = 50)
//    @NotNull
//    var name: String = ""
//
//
//    /** 宽度(mm) */
//    @Column
//    @NotNull
//    var width: Double = 0.0
//
//    /** 高度(mm) */
//    @Column
//    @NotNull
//    var height: Double = 0.0
//
//
//    @Column
//    @NotNull
//    var deleted: Boolean = false
//
//}


/** 相册中的单页 */
@Entity
@Table(name = "scene")
class Scene : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    /**
     * 索引，顺序
     */
    @Column
    var index: Int = 0

    /** 属于哪个相册 */
    @Column(name = "album_id", insertable = false, updatable = false)
    var albumId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    @NotNull
    lateinit var album: Template


    /** 模板ID */
    @Column(name = "template_id", insertable = false, updatable = false)
    var templateId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @NotNull
    lateinit var template: Template


    @Column
    @NotNull
    var deleted: Boolean = false

}
package com.unicolour.joyspace.model

import java.util.Calendar
import javax.persistence.*

import javax.validation.constraints.NotNull
import javax.persistence.JoinColumn
import javax.persistence.FetchType

@Entity
@Table(name = "user_image_file")
class UserImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @NotNull
    @Column(length = 50)
    lateinit var sessionId: String

    @NotNull
    @Column(length = 50)
    lateinit var fileName: String

    @NotNull
    @Column(length = 10)
    lateinit var type: String

    @Column
    @NotNull
    var userId: Int = 0

    @Column
    @NotNull
    lateinit var uploadTime: Calendar

    @Column
    @NotNull
    var width: Int = 0

    @Column
    @NotNull
    var height: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    var orderItem: PrintOrderItem? = null

    //region 缩略图
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_id")
    var thumbnail: UserImageFile? = null
    //endregion

}

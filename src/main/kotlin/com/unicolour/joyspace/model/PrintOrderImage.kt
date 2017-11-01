package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

//订单项用户图片记录
@Entity
@Table(name = "print_order_image")
class PrintOrderImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column
    @NotNull
    var orderId: Int = 0

    @Column
    @NotNull
    var orderItemId: Int = 0

    @Column(length = 50)
    @NotNull
    var name: String = ""

    //图片处理参数
    @Column(length = 1000)
    var processParams: String? = null

    //region 用户图片
    @Column(name = "user_image_file_id", insertable = false, updatable = false)
    var userImageFileId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_image_file_id")
    var userImageFile: UserImageFile? = null
    //endregion
}
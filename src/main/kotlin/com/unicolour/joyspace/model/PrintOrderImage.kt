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

    @Column
    @NotNull
    var userImageFileId: Int = 0

    //图片处理参数
    @Column(length = 1000)
    var processParams: String? = null
}
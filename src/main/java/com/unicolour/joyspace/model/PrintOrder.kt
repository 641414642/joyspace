package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "print_order")
class PrintOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    //属于哪个投放商
    @Column
    @NotNull
    var companyId: Int = 0

    //属于哪个用户
    @Column
    @NotNull
    var userId: Int = 0

    //在哪台自助机上输出
    @Column
    @NotNull
    var printStationId: Int = 0

    @NotNull
    @Column
    lateinit var createTime: Calendar

    @Column
    var state: Byte = PrintOrderState.CREATED.value

    @OneToMany(mappedBy = "printOrder")
    lateinit var printOrderItems: List<PrintOrderItem>
}

@Entity
@Table(name = "print_order_item")
class PrintOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    //region 订单
    /** 属于哪个订单 */
    @Column(name = "print_order_id", insertable = false, updatable = false)
    var printOrderId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "print_order_id")
    @NotNull
    lateinit var printOrder: PrintOrder
    //endregion

    //region 图片文件
    //图片文件id
    @Column(name = "user_image_file_id", insertable = false, updatable = false)
    @NotNull
    var userImageFileId: Int = 0

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_image_file_id")
    @NotNull
    lateinit var userImageFile: UserImageFile
    //endregion

    //产品id
    @Column
    @NotNull
    var productId: Int = 0

    //打印份数
    @Column
    @NotNull
    var copies: Int = 0
}

/** 订单状态 */
enum class PrintOrderState(val value: Byte) {
    CREATED(0),  //刚创建时的状态
    PAYED(1),    //用户已支付
    PRINTED(2)   //已打印
}

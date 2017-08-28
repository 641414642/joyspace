package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "print_order")
class PrintOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    //属于哪个投放商
    @Column
    @NotNull
    var companyId: Int = 0

    //属于哪个用户
    @Column
    @NotNull
    var userId: Int = 0

    //订单编号
    @Column(length = 32)
    @NotNull
    lateinit var orderNo: String

    //在哪台自助机上输出
    @Column
    @NotNull
    var printStationId: Int = 0

    @NotNull
    @Column
    lateinit var createTime: Calendar

    @NotNull
    @Column
    lateinit var updateTime: Calendar

    @Column
    var state: Byte = PrintOrderState.CREATED.value

    @OneToMany(mappedBy = "printOrder")
    lateinit var printOrderItems: List<PrintOrderItem>
}

@Entity
@Table(name = "print_order_item")
class PrintOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    //region 订单
    /** 属于哪个订单 */
    @Column(name = "print_order_id", insertable = false, updatable = false)
    var printOrderId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "print_order_id")
    var printOrder: PrintOrder? = null
    //endregion

    //图片文件
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderItem")
    lateinit var userImageFiles: List<UserImageFile>

    //region 产品
    @Column(name = "product_id", insertable = false, updatable = false)
    var productId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @NotNull
    lateinit var product: Product
    //endregion

    //打印份数
    @Column
    @NotNull
    var copies: Int = 0
}

/** 订单状态 */
enum class PrintOrderState(val value: Byte) {
    CREATED(0),  //刚创建时的状态
    PAYED(1),    //用户已支付
    DOWNLOADED(2), //已下载到自助机
    PRINTED(3),   //已打印
}

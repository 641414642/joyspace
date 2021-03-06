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

    //店面id
    @Column
    @NotNull
    var positionId: Int = 0

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

    //总价（单位分）
    @Column
    @NotNull
    var totalFee: Int = 0

    //折扣（单位分）
    @Column
    @NotNull
    var discount: Int = 0

    //使用的优惠券
    @Column
    var couponId: Int? = null

    //是否已支付
    @Column
    @NotNull
    var payed: Boolean = false

    //用户图片是否已上传
    @Column
    @NotNull
    var imageFileUploaded: Boolean = false

    //是否已下载到自助机
    @Column
    @NotNull
    var downloadedToPrintStation: Boolean = false

    //是否已打印
    @Column
    @NotNull
    var printedOnPrintStation: Boolean = false

    //是否已清除文件
    @Column
    @NotNull
    var imageFileCleared: Boolean = false

    //分账比例 x 1000
    @Column(name = "transfer_proportion")
    var transferProportion: Int = 1000

    //是否已经转账给投放商
    @Column
    @NotNull
    var transfered: Boolean = false

    @OneToMany(mappedBy = "printOrder")
    lateinit var printOrderItems: List<PrintOrderItem>

    @Column
    @NotNull
    var pageCount = 0     //打印页数

    //是否已取消订单
    @Column
    @NotNull
    var canceled: Boolean = false

    @Column
    var province: String? = null
    @Column
    var city: String? = null
    @Column
    var area: String? = null
    @Column
    var address: String? = null
    @Column
    var phoneNum: String? = null
    @Column
    var name: String? = null

    @Column
    @NotNull
    var printType = 0 //0:现场打印  1：邮寄

    @Column(length = 200)
    @NotNull
    var userName: String = ""     //下单用户名

    @Column(length = 50)
    @NotNull
    var companyName: String = ""     //投放商名称

    @Column(length = 50)
    @NotNull
    var positionName: String = ""     //店面名

    @Column(length = 50)
    @NotNull
    var printStationName: String = ""     //自助机名称

    @Column(length = 200)
    @NotNull
    var productNames: String = ""     //产品名称列表 (逗号分隔)

    @Column
    @NotNull
    var totalPageCount: Int = 0     //总打印张数

    @Column
    var transferTime: Calendar? = null   //微信转账时间

    @Column(length = 50)
    var transferReceiverName: String? = null    //微信转账收款人姓名

    @Column
    @NotNull
    var transferAmount: Int = 0     //微信转账金额

    @Column
    @NotNull
    var transferCharge: Int = 0     //微信转账手续费
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

    @Column
    @NotNull
    var productId: Int = 0

    @Column
    @NotNull
    var productType: Int = 0

    @Column(length = 10)
    @NotNull
    var productVersion: String = ""

    //打印份数
    @Column
    @NotNull
    var copies: Int = 0

    @OneToMany(mappedBy = "orderItemId")
    lateinit var orderImages: List<PrintOrderImage>

    //region 用户图片
    @Column(name = "user_image_file_id", insertable = false, updatable = false)
    var userImageFileId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_image_file_id")
    var userImageFile: UserImageFile? = null
    //endregion

    @Column
    var status: Int? = PrintOrderImageStatus.CREATED.value

    @Column
    @NotNull
    var pageCount: Int = 0   //打印张数
}




//订单中产品缩略图（用户上传）
@Entity
@Table(name = "print_order_product_image")
class PrintOrderProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column
    @NotNull
    var orderId: Int = 0

    @Column
    @NotNull
    var productId: Int = 0


    //region 用户图片
    @Column(name = "user_image_file_id", insertable = false, updatable = false)
    var userImageFileId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_image_file_id")
    var userImageFile: UserImageFile? = null
    //endregion

    @Column
    @NotNull
    var status: Int = PrintOrderImageStatus.CREATED.value
}

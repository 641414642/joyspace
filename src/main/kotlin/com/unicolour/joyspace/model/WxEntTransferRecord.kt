package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

//微信企业支付记录
@Entity
@Table(name = "wx_ent_transfer_record")
class WxEntTransferRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    //转到哪个投放商
    @Column
    @NotNull
    var companyId: Int = 0

    //收款openId
    @Column
    @NotNull
    lateinit var receiverOpenId: String

    //收款人姓名
    @Column(length = 50)
    @NotNull
    lateinit var receiverName: String

    //商户订单号
    @Column(length = 32)
    @NotNull
    lateinit var tradeNo: String

    //转账完成的时间
    @Column
    @NotNull
    lateinit var transferTime: Calendar

    //总金额(分)
    @Column
    @NotNull
    var amount: Int = 0
}

@Entity
@Table(name = "wx_ent_transfer_record_item")
class WxEntTransferRecordItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column
    @NotNull
    var recordId: Int = 0

    @Column
    @NotNull
    var printOrderId: Int = 0

    //手续费
    @Column
    @NotNull
    var charge: Int = 0

    //实际转账金额
    @Column
    @NotNull
    var amount: Int = 0
}

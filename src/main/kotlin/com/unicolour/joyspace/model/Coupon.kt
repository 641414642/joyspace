package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 优惠券 */
@Entity
@Table(name = "coupon")
class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column(length = 50)
    @NotNull
    var code: String = ""

    //最大使用次数(0表示无限制)
    @Column
    @NotNull
    var maxUses: Int = 0

    //每用户最大使用次数(0表示无限制)
    @Column
    @NotNull
    var maxUsesPerUser: Int = 0


    @Column
    var begin: Date? = null   //生效日期

    @Column
    var expire: Date? = null  //过期日期

    @Column
    @NotNull
    var enabled: Boolean = false   //是否可用

    @Column
    @NotNull
    var threshold: Int = 0        //满金额（分）

    @Column
    @NotNull
    var discount: Int = 0         //减金额（分）
}

/** 优惠券使用限制条件 */
@Entity
@Table(name = "coupon_constrains")
class CouponConstrains {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column
    @NotNull
    var couponId: Int = 0

    @Column
    @NotNull
    var constrainsType: Int = 0

    @Column
    @NotNull
    var value: Int = 0
}

enum class CouponConstrainsType(val value:Int, val dispName:String) {
    PRINT_STATION(1, "自助机"),
    POSITION(2, "投放地点"),
    COMPANY(3, "投放商"),   // 投放商 > 投放地点 > 自助机

    PRODUCT(4, "产品"),
    PRODUCT_TYPE(5, "产品类型"),  // 产品类型 > 产品

    USER_REG_DAYS(6, "用户注册时间长短(多少天)"),
    USER_GENDER(7, "用户性别"),   //1男，2女
}

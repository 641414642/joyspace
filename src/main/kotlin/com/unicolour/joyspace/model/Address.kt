package com.unicolour.joyspace.model

import javax.persistence.*
import javax.validation.constraints.NotNull

/** 用户优惠券列表 */
@Entity
@Table(name = "address")
class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column
    @NotNull
    var userId: Int = 0

    @Column
    var province: String? = null

    @Column
    var city: String? = null

    @Column
    var area: String? = null

    @Column
    var address: String? = null

    //联系电话
    @Column
    var phoneNum: String? = null

    //联系人
    @Column
    var name: String? = null


}
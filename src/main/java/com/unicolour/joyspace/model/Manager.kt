package com.unicolour.joyspace.model

import java.util.Calendar
import javax.persistence.*

import javax.validation.constraints.NotNull

@Entity
@Table(name = "manager")
class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @NotNull
    @Column(length = 200)
    lateinit var userName: String

    @Column(length = 80)
    var fullName: String? = null

    @Column
    var sex: Byte = 0     //0 男   1 女

    @Column(length = 80)
    var email: String? = null

    @Column(length = 150)
    var phone: String? = null

    @Column(length = 50)
    var cellPhone: String? = null

    @Column(length = 300)
    var address: String? = null

    @Column(length = 20)
    var qq: String? = null

    @Column(length = 10)
    var postcode: String? = null

    @NotNull
    @Column
    lateinit var createTime: Calendar

    @NotNull
    @Column(length = 128)
    lateinit var password: String

    @NotNull
    var isEnabled: Boolean = false

    //region 店面
    /** 店面ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @NotNull
    lateinit var company: Company
    //endregion
}

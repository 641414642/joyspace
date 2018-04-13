package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/** 自助机登录会话 */
@Entity
@Table(name = "print_station_login_session")
class PrintStationLoginSession {
    @Id
    @Column(length = 32)
    var id: String = ""

    //自助机Id
    @Column
    var printStationId: Int = 0

    //过期时间
    @Column
    lateinit var expireTime: Calendar

    @Column(length = 40)
    var uuid: String? = null
}
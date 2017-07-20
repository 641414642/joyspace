package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "verify_code")
class VerifyCode {
    @Id
    @Column(length = 20)
    lateinit var phoneNumber: String

    @NotNull
    @Column(length = 10)
    lateinit var code: String

    @Column
    @NotNull
    lateinit var sendTime: Calendar   //发送时间
}

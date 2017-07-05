package com.unicolour.joyspace.model

import java.util.Calendar
import javax.persistence.*

import javax.validation.constraints.NotNull

@Entity
@Table(name = "user_image_file")
class UserImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @NotNull
    @Column(length = 50)
    lateinit var sessionId: String

    @NotNull
    @Column(length = 50)
    lateinit var fileName: String

    @NotNull
    @Column(length = 10)
    lateinit var type: String

    @Column
    @NotNull
    var userId: Int = 0

    @Column
    @NotNull
    lateinit var uploadTime: Calendar

    @Column
    @NotNull
    var width: Int = 0

    @Column
    @NotNull
    var height: Int = 0
}

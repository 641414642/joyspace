package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/** 自助机任务 */
@Entity
@Table(name = "print_station_task")
class PrintStationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    @Column
    @NotNull
    var type: Int = 0

    @Column
    @NotNull
    var printStationId: Int = 0   //属于哪个自助机

    @NotNull
    @Column
    lateinit var createTime: Calendar

    @Column(length = 1000)
    @NotNull
    lateinit var param: String

    @Column
    @NotNull
    var fetched: Boolean = false
}

enum class PrintStationTaskType(val value: Int) {
    PROCESS_PRINT_ORDER(1),
    UPLOAD_LOG(2),
}
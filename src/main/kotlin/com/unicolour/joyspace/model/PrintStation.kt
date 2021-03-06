package com.unicolour.joyspace.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.persistence.JoinColumn
import javax.persistence.FetchType



/** 打印自助机 */
@Entity
@Table(name = "print_station")
class PrintStation {
    @Id
    var id: Int = 0

    /** 名称 */
    @Column(length = 50)
    @NotNull
    var name: String = ""

    /** 微信二维码 */
    @Column(length = 255)
    @NotNull
    var wxQrCode: String = ""

    @Column(name = "position_id", insertable = false, updatable = false)
    var positionId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    lateinit var position: Position

    @Column
    @NotNull
    var stationType: Int = 0            //站点属性

    @Column(length = 128)
    @NotNull
    lateinit var printerType: String     //允许的打印机类型

    @Column(length = 255)
    var printerModel: String? = null     //具体的打印机型号

    @Column
    var paperWidth: Double? = null       //当前的纸宽

    @Column
    var paperLength: Double? = null      //当前的纸长

    @Column
    var rollPaper: Boolean? = false      //是否是卷纸

    //region 店面
    /** 店面ID */
    @Column(name = "company_id", insertable = false, updatable = false)
    var companyId: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @NotNull
    lateinit var company: Company
    //endregion

    @Column(length = 100)
    var addressNation: String? = null

    @Column(length = 100)
    var addressProvince: String? = null

    @Column(length = 100)
    var addressCity: String? = null

    @Column(length = 100)
    var addressDistrict: String? = null

    @Column(length = 100)
    var addressStreet: String? = null

    @Column(name = "status")
    var status: Int = PrintStationStatus.NORMAL.value

    //region 广告
    /** 广告ID */
    @Column(name = "ad_set_id", insertable = false, updatable = false)
    var adSetId: Int? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_set_id")
    var adSet: AdSet? = null
    //endregion

    //分账比例 x 1000
    @Column(name = "transfer_proportion")
    var transferProportion: Int = 1000

    //上次登录时的home版本号
    @Column(name = "last_login_version")
    var lastLoginVersion: Int? = null

    //首次登录的时间
    @Column
    var firstLoginTime: Calendar? = null

    //上次登录的时间
    @Column
    var lastLoginTime: Calendar? = null

    //上次访问的时间
    @Column
    var lastAccessTime: Calendar? = null

    //指定更新到的home版本号, 如果为null, 使用全局版本号设置
    @Column(name = "update_to_version")
    var updateToVersion: Int? = null

    @Column(length = 32)
    var uuid: String? = null

    @Column
    var loginSequence: Int? = 0
}

enum class PrintStationStatus(val value:Int, val message:String) {
    NORMAL(0, "正常"),
    PRINTER_OFFLINE(1, "打印机脱机"),
    OUT_OF_PRINTING_SUPPLIES(2, "打印耗材用完")
}


//站点属性
enum class StationType(
        val value: Int,
        val displayName: String
) {
    DEFAULT(0, ""),
    JOYSPACE(1, "悦印自助机"),
    CY(2, "CY单机"),
    D700(3, "D700单机"),
    EPSON_DESKTOP(4, "EPSON桌面机"),
    F2180(5, "F2180"),
    OTHER(6, "其它设备"),
}

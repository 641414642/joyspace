package com.unicolour.joyspace.util

import java.util.*


/**
 * 格式化日期对象 (yyyy-MM-dd HH:mm:ss)
 * @return
 */
fun Calendar?.format() : String? {
    return if (this == null) null else String.format("%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS", this)
}

/**
 * 格式化日期对象 (yyyy-MM-dd HH:mm:ss.SSS)
 * @return
 */
fun Calendar?.formatWithMillisecond() : String? {
    return if (this == null) null else String.format("%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL", this)
}
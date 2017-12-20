package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.dao.PrintStationLoginSessionDao
import com.unicolour.joyspace.model.PrintStationStatus.OFFLINE
import com.unicolour.joyspace.model.PrintStationStatus.WORKING
import com.unicolour.joyspace.service.ScheduledTasks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional


@Component
open class ScheduledTasksImpl : ScheduledTasks {
    @Autowired
    lateinit var printStationLoginSessionDao: PrintStationLoginSessionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)  //1分钟更新一次自助机状态
    @Transactional
    override fun updatePrintStationStatus() {
        printStationDao.findAll().forEach({
            if (it.status == WORKING.value || it.status == OFFLINE.value) {
                var offline = true
                val printStationLoginSession = printStationLoginSessionDao.findByPrintStationId(it.id)
                if (printStationLoginSession != null) {
                    val expireTime = printStationLoginSession.expireTime.timeInMillis

                    val time = Calendar.getInstance()
                    time.add(Calendar.SECOND, 3600 - 60)

                    if (expireTime > time.timeInMillis) {    //自助机1分钟之内访问过后台
                        offline = false
                    }
                }

                if (offline && it.status == WORKING.value ||
                        !offline && it.status == OFFLINE.value) {
                    it.status = if (offline) OFFLINE.value else WORKING.value
                    printStationDao.save(it)
                }
            }
        })
    }
}


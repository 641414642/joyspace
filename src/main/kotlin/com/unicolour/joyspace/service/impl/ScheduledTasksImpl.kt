package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.PrintOrderDao
import com.unicolour.joyspace.service.ScheduledTasks
import com.unicolour.joyspace.util.format
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import javax.transaction.Transactional


@Component
open class ScheduledTasksImpl : ScheduledTasks {
    companion object {
        val logger = LoggerFactory.getLogger(ScheduledTasksImpl::class.java)
    }

    @Autowired
    lateinit var printOrderDao: PrintOrderDao

    @Value("\${com.unicolour.joyspace.assetsDir}")
    lateinit var assetsDir: String

    //删除旧用户图片
    @Scheduled(cron = "0 */5 2-3 * * *")
    @Transactional
    override fun clearOldUserImages() {
        val beforeTime = Calendar.getInstance()
        beforeTime.add(Calendar.DAY_OF_MONTH, -30)

        val orders = printOrderDao.getOldUnClearedPrintOrders(beforeTime, 1000)
        var clearedOrderCount = 0
        var clearedFileCount = 0
        var clearedFileSizeSum = 0L

        logger.info("Begin clear image files of old orders, beforeTime:${beforeTime.format()}")

        for (order in orders) {
            try {
                val orderItems = order.printOrderItems
                for (orderItem in orderItems) {
                    for (orderImage in orderItem.orderImages) {
                        if (orderImage.userImageFile != null) {
                            val userImgFile = orderImage.userImageFile!!
                            val filePath = "user/${userImgFile.userId}/${userImgFile.sessionId}/${userImgFile.fileName}.${userImgFile.type}"
                            val file = File(assetsDir, filePath)

                            if (file.exists()) {
                                val fileLen = file.length()

                                if (file.delete()) {
                                    logger.info("delete file: ${file.absolutePath}")

                                    clearedFileCount ++
                                    clearedFileSizeSum += fileLen
                                }
                            }
                        }
                    }
                }

                order.imageFileCleared = true
                printOrderDao.save(order)

                clearedOrderCount++

                logger.info("Done clear image files of order, id: ${order.id}, updateTime:${order.updateTime.format()}")
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }

        logger.info("Done clear image files of old orders, beforeTime:${beforeTime.format()}, clearedOrderCount: $clearedOrderCount, clearedFileCount: $clearedFileCount, clearedFileSize: ${clearedFileSizeSum / 1024.0/ 1024.0} MB")
    }
}

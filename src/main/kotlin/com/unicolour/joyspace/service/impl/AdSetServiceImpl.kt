package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.AdSetDao
import com.unicolour.joyspace.model.AdImageFile
import com.unicolour.joyspace.model.AdSet
import com.unicolour.joyspace.service.AdSetService
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
open class AdSetServiceImpl : AdSetService {
    @Autowired
    lateinit var adSetDao: AdSetDao

    @Autowired
    lateinit var managerService : ManagerService

    override fun getAdImageUrl(baseUrl:String, adImageFile: AdImageFile): String {
        return "$baseUrl/assets/ad/${adImageFile.adSet.id}/images/${adImageFile.fileName}.${adImageFile.fileType}"
    }

    @Transactional
    override fun createAdSet(name: String) {
        val loginManager = managerService.loginManager
        val now = Calendar.getInstance()

        val adSet = AdSet()
        adSet.name = name
        adSet.createTime = now
        adSet.updateTime = now
        adSet.imageCount = 0
        adSet.companyId = loginManager!!.companyId

        adSetDao.save(adSet)
    }

    @Transactional
    override fun updateAdSet(id: Int, name: String): Boolean {
        val adSet = adSetDao.findOne(id)
        if (adSet == null) {
            return false
        }
        else {
            adSet.name = name
            adSet.updateTime = Calendar.getInstance()

            adSetDao.save(adSet)
            return true
        }
    }
}
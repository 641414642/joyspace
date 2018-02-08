package com.unicolour.joyspace.config

import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.PositionDao
import com.unicolour.joyspace.dao.PrintStationDao
import com.unicolour.joyspace.service.CompanyService
import com.unicolour.joyspace.service.ManagerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class ApplicationStartup : ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var positionDao: PositionDao

    @Autowired
    lateinit var printStationDao: PrintStationDao

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        //创建缺省店面和管理员
        val companies = companyDao.findAll()
        if (!companies.iterator().hasNext()) {
            companyService.createCompany("缺省店面", null, "admin", "管理员", "", "", "123456")
        }
    }
}
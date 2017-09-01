package com.unicolour.joyspace.config

import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.dao.ManagerDao
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.service.CompanyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import com.unicolour.joyspace.service.ManagerService

@Component
class ApplicationStartup : ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    lateinit var managerDao: ManagerDao

    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var companyService: CompanyService

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        //创建缺省店面和管理员
        val companies = companyDao.findAll()
        if (!companies.iterator().hasNext()) {
            companyService.createCompany("缺省店面", null, "admin", "管理员", "", "", "123456")
        }
    }
}
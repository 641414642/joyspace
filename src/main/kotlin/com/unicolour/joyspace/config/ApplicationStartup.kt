package com.unicolour.joyspace.config

import com.unicolour.joyspace.dao.*
import com.unicolour.joyspace.model.City
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.Manager
import com.unicolour.joyspace.model.Position
import com.unicolour.joyspace.service.CompanyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

import com.unicolour.joyspace.service.ManagerService

@Component
class ApplicationStartup : ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    lateinit var companyDao: CompanyDao

    @Autowired
    lateinit var managerService: ManagerService

    @Autowired
    lateinit var companyService: CompanyService

    @Autowired
    lateinit var cityDao: CityDao

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

        //缺省城市(北京)
        val cities = cityDao.findAll()
        if (!cities.iterator().hasNext()) {
            val defCity = City()
            defCity.name = "北京"
            defCity.minLatitude = 39.550648
            defCity.maxLatitude = 40.313043
            defCity.minLongitude = 115.911255
            defCity.maxLongitude = 116.993408

            cityDao.save(defCity)
        }
    }
}
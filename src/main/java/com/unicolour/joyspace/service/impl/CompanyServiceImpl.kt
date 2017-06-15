package com.unicolour.joyspace.service.impl

import com.unicolour.joyspace.dao.CompanyDao
import com.unicolour.joyspace.model.Company
import com.unicolour.joyspace.model.PriceList
import com.unicolour.joyspace.service.CompanyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class CompanyServiceImpl : CompanyService {
    @Autowired
    lateinit var companyDao: CompanyDao

    override fun createCompany(name: String, defPriceList: PriceList?): Company {
        val company = Company();

        company.name = name;
        company.createTime = Calendar.getInstance()
        company.defaultPriceList = defPriceList

        companyDao.save(company)

        return company
    }

    override fun updateCompany(companyId: Int, name: String): Boolean {
        val company = companyDao.findOne(companyId)
        if (company != null) {
            company.name = name
            companyDao.save(company)

            return true;
        }
        else {
            return false;
        }
    }
}
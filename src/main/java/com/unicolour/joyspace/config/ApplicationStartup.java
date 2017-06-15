package com.unicolour.joyspace.config;

import com.unicolour.joyspace.dao.CompanyDao;
import com.unicolour.joyspace.dao.ManagerDao;
import com.unicolour.joyspace.model.Company;
import com.unicolour.joyspace.model.Manager;
import com.unicolour.joyspace.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.unicolour.joyspace.service.ManagerService;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
	@Autowired
	ManagerDao managerDao;

	@Autowired
	CompanyDao companyDao;

	@Autowired
	ManagerService managerService;

	@Autowired
	CompanyService companyService;

	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		//创建缺省店面
		Iterable<Company> companies = companyDao.findAll();
		if (!companies.iterator().hasNext()) {
			companyService.createCompany("缺省店面", null);
		}

		//创建管理员用户
		Iterable<Manager> managers = managerDao.findAll();
		if (!managers.iterator().hasNext()) {
			Company defCompany = companyDao.findAll().iterator().next();
			managerService.createManager("admin", "123456", "管理员", "", defCompany);
		}
	}
}
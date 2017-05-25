package com.unicolour.joyspace.config;

import com.unicolour.joyspace.dao.ManagerDao;
import com.unicolour.joyspace.model.Manager;
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
	ManagerService managerService;

	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		//创建管理员用户
		Iterable<Manager> managers = managerDao.findAll();
		if (!managers.iterator().hasNext()) {
			managerService.createUser("admin", "123456", "管理员", "");
		}
	}
}
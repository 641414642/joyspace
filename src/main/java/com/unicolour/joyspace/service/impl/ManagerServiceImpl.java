package com.unicolour.joyspace.service.impl;

import com.unicolour.joyspace.dao.ManagerDao;
import com.unicolour.joyspace.dto.LoginManagerDetail;
import com.unicolour.joyspace.model.Manager;
import com.unicolour.joyspace.service.ManagerService;
import com.unicolour.joyspace.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Calendar;

@Service
public class ManagerServiceImpl implements ManagerService {
	@Autowired
	ManagerDao managerDao;
	
	@Autowired
	PasswordEncoder passwordEncoder;

	private static final String OLD_USER_PASSWORD_KEY = "%^TSD#$#DSRygf46";
	private static final String OLD_MANAGER_PASSWORD_KEY = "cd433d$%^B^ffdH";

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Manager manager = managerDao.findByUserNameOrFullName(username, username);
		if (manager != null) {
			return new LoginManagerDetail(
				manager.getId(), manager.getCreateTime(), manager.getFullName(), manager.getUserName(), manager.getPassword(), manager.isEnabled(),
				true, true, true, Arrays.asList(new SimpleGrantedAuthority("USER")));
		}
		else {
			throw new UsernameNotFoundException(username + " not found.");
		}
	}

	@Override
	public Manager createUser(String userName, String password, String fullName, String cellPhone) {
		Manager manager = new Manager();
		
		manager.setUserName(userName);
		manager.setFullName(fullName);
		manager.setPassword(passwordEncoder.encode(password));
		manager.setCellPhone(cellPhone);
		manager.setCreateTime(Calendar.getInstance());
		manager.setEnabled(true);
		
		managerDao.save(manager);
		
		//operationLogService.addOperationLog("创建用户：" + manager.getUserName());
		
		return manager;
	}

	@Override
	public Manager editUser(int id, String fullName, String cellPhone) {
		Manager manager = managerDao.findById(id);

		if (manager != null) {
			manager.setFullName(fullName);
			manager.setCellPhone(cellPhone);
			managerDao.save(manager);

			//operationLogService.addOperationLog("修改用户：" + manager.getUserName());
			return manager;
		}
		else {
			return null;
		}
	}

	@Override
	public boolean resetPassword(int userId, String password) {
		Manager manager = managerDao.findById(userId);
		
		if (manager != null) {
			manager.setPassword(passwordEncoder.encode(password));
			managerDao.save(manager);
			
			//operationLogService.addOperationLog("重置密码：" + manager.getUserName());
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Manager login(String userName, String password) {
		Manager manager = managerDao.findByUserName(userName);
		if (manager != null) {
			if (passwordEncoder.matches(password, manager.getPassword())) {
				return manager;
			}
			else if (passwordEncoder.matches(oldPasswordEncode(password), manager.getPassword())) {
				return manager;
			}
		}

		return null;
	}

	private CharSequence oldPasswordEncode(String password) {
		return Utils.calcMD5Hash(password + OLD_MANAGER_PASSWORD_KEY).toLowerCase();
	}
}

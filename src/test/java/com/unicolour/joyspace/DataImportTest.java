package com.unicolour.joyspace;

import com.unicolour.joyspace.dao.ManagerDao;
import com.unicolour.joyspace.model.Manager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.sql.*;
import java.util.Calendar;

@RunWith(SpringRunner.class)
@SpringBootTest
//@TestPropertySource(locations="classpath:test.properties")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataImportTest {
	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	ManagerDao managerDao;

	/** 导入管理员测试 */
	@Test
	public void importAdminUsers() throws IOException, SQLException {
		DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());

		String dbURL = "jdbc:sqlserver://localhost;databaseName=EPSON";
		String pass = "Uni1Colour2";
		try (Connection conn = DriverManager.getConnection(dbURL, "sa", pass)) {
			try (Statement stat = conn.createStatement()) {
				ResultSet rs = stat.executeQuery("select * from Managerinfo_Basic");
				while (rs.next()) {
					Manager manager = new Manager();

					int id = rs.getInt("ManagerId");
					String userName = rs.getString("ManagerName");
					String fullName = rs.getString("ManagerRealName");

					manager.setUserName(userName);
					manager.setFullName(fullName);
					manager.setEnabled(rs.getBoolean("IsEnable"));
					manager.setAddress(rs.getString("ManagerAddress"));
					manager.setCellPhone(rs.getString("ManagerMobile"));
					manager.setPhone(rs.getString("ManagerTele"));
					manager.setEmail(rs.getString("Email"));

					Calendar cal = Calendar.getInstance();
					Time createTime = rs.getTime("CreateDate", cal);
					cal.setTime(createTime);
					manager.setCreateTime(cal);

					manager.setSex(rs.getByte("ManagerSex"));
					manager.setQq(rs.getString("ManagerQQ"));
					manager.setPassword(passwordEncoder.encode(rs.getString("PassWord")));

					managerDao.save(manager);
				}
			}
		}
	}
}

package com.unicolour.joyspace.dto;

import java.util.Calendar;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class LoginManagerDetail extends User {
	private static final long serialVersionUID = 2697760667638615654L;

	private final String fullName;
	private final int userId;
	private Calendar createTime;

	public LoginManagerDetail(int userId, Calendar createTime, String fullName, String username, String password, boolean enabled, boolean accountNonExpired,
							  boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.userId = userId;
		this.fullName = fullName;
		this.createTime = createTime;
	}
	
	public int getUserId() {
		return userId;
	}

	public String getFullName() {
		return fullName;
	}

	public Calendar getCreateTime() {
		return createTime;
	}
}

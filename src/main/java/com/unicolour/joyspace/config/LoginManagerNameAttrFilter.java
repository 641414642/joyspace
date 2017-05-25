package com.unicolour.joyspace.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.unicolour.joyspace.dto.LoginManagerDetail;
import com.unicolour.joyspace.util.Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LoginManagerNameAttrFilter implements Filter {
	
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Object principal = auth == null ? null : auth.getPrincipal();
    LoginManagerDetail userDetail = null;

    if (principal != null && principal instanceof LoginManagerDetail) {
      userDetail = (LoginManagerDetail) principal;
    }

    String dispName = userDetail == null || Utils.isNullOrEmpty(userDetail.getFullName()) ? auth.getName() : userDetail.getFullName();
    String regTime = userDetail == null ? null : Utils.formatTime(userDetail.getCreateTime());

    req.setAttribute("LoginUserDispName", dispName);
    req.setAttribute("LoginUserName", auth.getName());
    req.setAttribute("LoginUserRegTime", regTime);

    chain.doFilter(req, res);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(FilterConfig cfg) throws ServletException {
  }
}
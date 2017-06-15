package com.unicolour.joyspace.controller;

import com.unicolour.joyspace.dao.CompanyDao;
import com.unicolour.joyspace.dto.LoginManagerDetail;
import com.unicolour.joyspace.model.Company;
import com.unicolour.joyspace.model.Manager;
import com.unicolour.joyspace.service.CompanyService;
import com.unicolour.joyspace.util.Pager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CompanyController {
	private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

	@Autowired
	CompanyDao companyDao;
	
	@Autowired
	CompanyService companyService;
	
	@RequestMapping("/company/list")
	public ModelAndView companyList(
			ModelAndView modelAndView,
			@RequestParam(name="name", required=false, defaultValue="") String name,
			@RequestParam(name="pageno", required=false, defaultValue="1") int pageno) {

		Pageable pageable = new PageRequest(pageno - 1, 20);
		Page<Company> users = ((name == null) || name.equals("")) ?
				companyDao.findAll(pageable) :
				companyDao.findByName(name, pageable);

		modelAndView.getModel().put("inputCompanyName", name);

		Pager pager = new Pager(users.getTotalPages(), 7, pageno-1);
		modelAndView.getModel().put("pager", pager);

		modelAndView.getModel().put("users", users.getContent());

		modelAndView.getModel().put("viewCat", "user_mgr");
		modelAndView.getModel().put("viewContent", "company_list");
		modelAndView.setViewName( "layout");

		return modelAndView;
	}
}
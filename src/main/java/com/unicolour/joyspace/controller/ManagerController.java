package com.unicolour.joyspace.controller;

import com.unicolour.joyspace.dao.ManagerDao;
import com.unicolour.joyspace.dto.LoginManagerDetail;
import com.unicolour.joyspace.model.Manager;
import com.unicolour.joyspace.service.ManagerService;
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
public class ManagerController {
	private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

	@Autowired
	ManagerDao managerDao;
	
	@Autowired
	ManagerService managerService;
	
	@RequestMapping("/manager/list")
	public ModelAndView adminUserList(
			ModelAndView modelAndView,
			@RequestParam(name="name", required=false, defaultValue="") String name,
			@RequestParam(name="pageno", required=false, defaultValue="1") int pageno) {

//		String reqPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

		Pageable pageable = new PageRequest(pageno - 1, 20);
		Page<Manager> users = name == null || name.equals("") ?
				managerDao.findAll(pageable) :
				managerDao.findByUserNameOrFullName(name, pageable);

		modelAndView.getModel().put("inputUserName", name);

		Pager pager = new Pager(users.getTotalPages(), 7, pageno-1);
		modelAndView.getModel().put("pager", pager);

		modelAndView.getModel().put("users", users.getContent());

		modelAndView.getModel().put("viewCat", "admin_mgr");
		modelAndView.getModel().put("viewContent", "manager_list");
		modelAndView.setViewName( "layout");

		return modelAndView;
	}

	@RequestMapping(path="/manager/change_pass", method=RequestMethod.GET)
	public String changePassword() {
		return "/manager/change_pass :: content";
	}

	@RequestMapping(path="/manager/change_pass", method=RequestMethod.POST)
	@ResponseBody
	public boolean changePassword(@RequestParam(name="newPass", required=true) String newPassword) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof LoginManagerDetail) {
			LoginManagerDetail userDetail = (LoginManagerDetail) auth.getPrincipal();
			return managerService.resetPassword(userDetail.getUserId(), newPassword);
		}
		else {
			return false;
		}
	}
}
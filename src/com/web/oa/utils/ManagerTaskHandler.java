package com.web.oa.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.service.EmployeeService;

public class ManagerTaskHandler implements TaskListener{

	@Override
	public void notify(DelegateTask delegate) {
		//在非spring环境下获取spring
		WebApplicationContext applicationContext = 
				ContextLoader.getCurrentWebApplicationContext();
		//从容器中获取employee对象
		EmployeeService employeeService = 
				(EmployeeService) applicationContext.getBean("employeeService");
		//在非spring环境中获取session，不能直接获取
		//可直接先获取request，
		HttpServletRequest request = 
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		//再从request中获取session
		//HttpSession session = request.getSession();
		//从session中获取当前登录对象
		//Employee employee = (Employee) session.getAttribute(Constans.GLOBLE_USER_SESSION);
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		//从当前登录对象中获取managerId
		Long managerId = activeUser.getManagerId();
		//根据managerId查询员工上级对象
		Employee employeeManager = employeeService.findByManagerId(managerId);
		//从查询出的上级对象中取出用户名，设置给待办人
		delegate.setAssignee(employeeManager.getName());
	}

}

package com.web.oa.service;

import java.util.List;

import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.SysUserRole;

public interface EmployeeService {

	Employee findEmployeeByName(String username);
	
	//根据managerID查询上级
	Employee findByManagerId(long managerId);
	
	List<EmployeeCustom> findUserAndRoleList();
	
	List<Employee> findUsers();
	
	void updateEmployeeRole(String roleId,String userId);
	
	List<Employee> findEmployeeByLevel(int level);
	
	int saveUser(Employee employee);
}

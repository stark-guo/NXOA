package com.web.oa.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.oa.mapper.EmployeeMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysUserRoleMapper;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.EmployeeExample;
import com.web.oa.pojo.EmployeeExample.Criteria;
import com.web.oa.pojo.SysUserRole;
import com.web.oa.pojo.SysUserRoleExample;
import com.web.oa.service.EmployeeService;

@Service("employeeService")
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	private EmployeeMapper employeeMapper;
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	@Autowired
	private SysUserRoleMapper userRoleMapper;
	
	@Override
	public Employee findEmployeeByName(String username) {
		EmployeeExample example = new EmployeeExample();
		Criteria criteria = example.createCriteria();
		criteria.andNameEqualTo(username);
		List<Employee> list = this.employeeMapper.selectByExample(example);
		
		if(list!=null && list.size()>0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public Employee findByManagerId(long managerId) {
		Employee employee = employeeMapper.selectByPrimaryKey(managerId);
		return employee;
	}

	@Override
	public List<EmployeeCustom> findUserAndRoleList() {
		return sysPermissionMapperCustom.findUserAndRoleList();
	}

	@Override
	public List<Employee> findUsers() {
		// TODO Auto-generated method stub
		return employeeMapper.selectByExample(null);
	}

	@Override
	public void updateEmployeeRole(String roleId, String userId) {
		// TODO Auto-generated method stub
		SysUserRoleExample example = new SysUserRoleExample();
		com.web.oa.pojo.SysUserRoleExample.Criteria criteria = example.createCriteria();
		criteria.andSysUserIdEqualTo(userId);
	
		List<SysUserRole> list = userRoleMapper.selectByExample(example);
		SysUserRole userRole = null;
		if(list!=null && list.size()>0) {
			 userRole = list.get(0);
		}
		userRole.setSysRoleId(roleId);
		
		userRoleMapper.updateByPrimaryKey(userRole);
	}
	
	//根据员工级别查找员工信息
	@Override
	public List<Employee> findEmployeeByLevel(int level) {
		// TODO Auto-generated method stub
		EmployeeExample example = new EmployeeExample();
		Criteria criteria = example.createCriteria();
		criteria.andRoleEqualTo(level);
		List<Employee> list = employeeMapper.selectByExample(example);
		return list;
	}

	@Override
	public int saveUser(Employee employee) {
		return employeeMapper.insert(employee);
	}

}

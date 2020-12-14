package com.web.oa.test;

import java.util.List;

import com.web.oa.pojo.Employee;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.impl.EmployeeServiceImpl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;

public class MainTest {
public static void main(String[] args) {
			fun2();
		}

public static void fun2() {
	Employee employee = new Employee();
	EmployeeService e = new EmployeeServiceImpl();
	List<Employee> list = e.findUsers();
	if(list!=null&&list.size()>0) {
		Employee employee2 = list.get(0);
		System.out.println(employee2.getPassword());
	}
}


public static void fun1() {
	ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(100, 20);
	String code = captcha.getCode();
	System.out.println(code);
}
}




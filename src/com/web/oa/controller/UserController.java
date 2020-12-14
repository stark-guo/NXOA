package com.web.oa.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.omg.CORBA.portable.UnknownException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysUserRole;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.SysService;

@Controller
public class UserController {
	
	
	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private SysService sysService;
	
	@RequestMapping("/login")
	public String login(HttpServletRequest request,Model model) throws Exception {
		
		String exceptionName = (String) request.getAttribute("shiroLoginFailure");
		System.out.println("=================="+exceptionName+"=================");
		if(exceptionName!=null) {
			if(UnknownAccountException.class.getName().equals(exceptionName)) {
				model.addAttribute("errorMsg","用户名不存在");
			}else if(IncorrectCredentialsException.class.getName().endsWith(exceptionName)) {
				model.addAttribute("errorMsg","密码错误");
			}else if("validataCodeMessage".equals(exceptionName)) {
				model.addAttribute("errorMsg","验证码错误");
			}else {
				model.addAttribute("errorMsg", "未知错误");
			}
		}
		return "login";
	}
	
	@RequestMapping("/findUserList")
	public ModelAndView findUserList(String userId) {
		ModelAndView mav = new ModelAndView();
		List<SysRole> allRoles = sysService.findAllRoles();
		List<EmployeeCustom> userlist = employeeService.findUserAndRoleList();
		
		mav.addObject("userList", userlist);
		mav.addObject("allRoles", allRoles);
		
		mav.setViewName("userlist");
		
		return mav;
	}
	
	@RequestMapping("/assignRole")
	@ResponseBody  //ajax回调
	public Map<String,String> assignRole(String roleId,String userId){
		Map<String,String> map = new HashMap<>();
		try {
			employeeService.updateEmployeeRole(roleId, userId);
			map.put("msg", "分配权限成功");
		}catch(Exception e) {
			e.printStackTrace();
			map.put("msg", "分配权限失败");
		}
		
		return map;
	}
	
	@RequestMapping("/toAddRole")
	public ModelAndView toAddRole() {
		ModelAndView mav = new ModelAndView();
		List<MenuTree> permissions = sysService.loadMenuTree();
		List<SysPermission> menus = sysService.findAllMenus();
		List<SysRole> permissionList = sysService.findRolesAndPermissions();
		
		mav.addObject("allPermissions", permissions);
		mav.addObject("menuTypes", menus);
		mav.addObject("roleAndPermissionsList", permissionList);
		mav.setViewName("rolelist");
		
		return mav;
	}
	
	@RequestMapping("/findRoles")
	public ModelAndView findRoles() {
		ModelAndView mav = new ModelAndView();
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<SysRole> allRoles = sysService.findAllRoles();
		List<MenuTree> allMenuAndPermision = sysService.getAllMenuAndPermision();
		mav.addObject("allRoles", allRoles);
		mav.addObject("activeUser",activeUser);
		mav.addObject("allMenuAndPermissions", allMenuAndPermision);
		
		mav.setViewName("permissionlist");
		return mav;
	}
	
	@RequestMapping("/saveRoleAndPermissions")
	public String saveRoleAndPermissions(SysRole role,int[] permissionIds) {
		String uuid = UUID.randomUUID().toString();
		role.setId(uuid);
		role.setAvailable("1");
		sysService.addRoleAndPermissions(role, permissionIds);
		return "redirect:/toAddRole";
	}
	
	@RequestMapping("/saveSubmitPermission")
	public String saveSubmitPermission(SysPermission permission) {
		if(permission.getAvailable()==null) {
			permission.setAvailable("0");
		}
		sysService.addSysPermission(permission);
		return "redirect:/toAddRole";
	}
	
	@RequestMapping("/viewPermissionByUser")
	@ResponseBody //ajax回调
	public SysRole viewPermissionByUser(String userName) {
		SysRole sysRole = sysService.findRolesAndPermissionsByUserId(userName);
		
		return sysRole;
	}
	
	@RequestMapping("/findNextManager")
	@ResponseBody
	public List<Employee> findNextManager(Integer level){
		level ++;
		List<Employee> employeeList = employeeService.findEmployeeByLevel(level);
		return employeeList;
	}
	 
	@RequestMapping("/saveUser")
	public String saveUser(Employee employee) {
		System.out.println("==============="+employee+"==============");
		//ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();		
		
		Md5Hash md5 = new Md5Hash(employee.getPassword(), "eteokues",2);
		employee.setId(null);
		employee.setPassword(md5.toString());
		employee.setSalt("eteokues");
		employeeService.saveUser(employee);
		
		SysUserRole userRole = new SysUserRole();
		List<SysUserRole> list = sysService.findUserRole();
		Integer i = 1;
		Integer id = 0;
		if(list!=null && list.size()>0) {
			for (SysUserRole sysUserRole : list) {
				 id = Integer.parseInt(sysUserRole.getId());
				 if(i==id) {
					 i++;
				 }
			}
		}
		userRole.setId(i.toString());
		userRole.setSysUserId(employee.getName());
		userRole.setSysRoleId(employee.getRole().toString());
		sysService.saveUserRole(userRole);
		
		return "redirect:/findUserList";
	}
	
	@RequestMapping("/loadMyPermissions")
	@ResponseBody
	public List<SysPermission> loadMyPermissions(String roleId){
		List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);
		
		for (SysPermission sysPermission : list) {
			System.out.println(sysPermission.getId()+","+sysPermission.getType()+"\n"+sysPermission.getName() + "," + sysPermission.getUrl()+","+sysPermission.getPercode());
		}
		return list;
	}
	
	@RequestMapping("/updateRoleAndPermission")
	public String updateRoleAndPermission(String roleId,int[] permissionIds) {
		sysService.updateRoleAndPermissions(roleId, permissionIds);
		return "redirect:/findRoles";		
	}
	
	@RequestMapping("/deleteRole")
	public String deleteRole(String roleId) {
		sysService.deleteRole(roleId);
		
		return "redirect:/findRoles";
	}
	
	/**
	 * 注销
	 * @param session
	 * @return
	 */
	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		//清空session
		session.invalidate();
		return "redirect:login.jsp";
	}
	
	@RequestMapping(value="/loginUser")
	public void loginUser(HttpServletRequest request,HttpServletResponse response,HttpSession session){
		response.setContentType("text/html;charset=utf-8");
		try {
			PrintWriter writer = response.getWriter();
			//Employee employee = (Employee) session.getAttribute("globle_user");
			ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
			String name = activeUser.getUsername();
			writer.write(name);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package com.web.oa.shiro;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.SysService;

public class CustomRealm extends AuthorizingRealm{

	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private SysService sysService;
	//登录认证
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String username = (String) token.getPrincipal();
		Employee employee = employeeService.findEmployeeByName(username);
		if(employee==null) {
			return null;
		}
		ActiveUser user = new ActiveUser();
		user.setId(employee.getId());
		user.setUserid(employee.getName());
		user.setUsercode(employee.getName());
		user.setUsername(employee.getName());
		user.setManagerId(employee.getManagerId());
		
		List<MenuTree> menuTree = sysService.loadMenuTree();
		if(menuTree!=null && !menuTree.equals("")) {
			user.setMenuTree(menuTree);
		}
		
		String password = employee.getPassword();
		String salt = employee.getSalt();
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user, password, ByteSource.Util.bytes(salt), "CustomRealm");
		return authenticationInfo;
	}

	//授权
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection coll) {
		ActiveUser activeUser = (ActiveUser) coll.getPrimaryPrincipal();
		List<SysPermission> permissions = null;
		try {
			permissions = sysService.findPermissionListByUserId(activeUser.getUsername());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> permissionList = new ArrayList<>();
		for (SysPermission permission : permissions) {
			permissionList.add(permission.getPercode());
		}
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.addStringPermissions(permissionList);
		return authorizationInfo;
	}

	
}

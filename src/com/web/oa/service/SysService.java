package com.web.oa.service;

import java.util.List;

import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysUserRole;

public interface SysService {

	List<MenuTree> loadMenuTree();
	
	//根据用户id查询权限范围的菜单
	List<SysPermission> findMenuListByUserId(String userid) throws Exception;
		
	//根据用户id查询权限范围的url
	List<SysPermission> findPermissionListByUserId(String userid) throws Exception;
	
	List<SysRole> findAllRoles();
	
	SysRole findRolesAndPermissionsByUserId(String userId);
	
	void addRoleAndPermissions(SysRole role,int[] permissionIds);
	
	//查询所有menu类permission
	List<SysPermission> findAllMenus();
	
	void addSysPermission(SysPermission permission);
	
	//根据用户ID查询其所有的菜单和权限
	List<SysPermission> findMenuAndPermissionByUserId(String userId);
	List<MenuTree> getAllMenuAndPermision();
	
	//根据角色ID查询权限
	List<SysPermission> findPermissionsByRoleId(String roleId);
	
	void updateRoleAndPermissions(String roleId,int[] permissionIds);

	List<SysRole> findRolesAndPermissions();
	
	int saveUserRole(SysUserRole UserRole);
	
	List<SysUserRole> findUserRole();
	
	void deleteRole(String roleId);
}

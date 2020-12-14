package com.web.oa.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.oa.mapper.SysPermissionMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysRoleMapper;
import com.web.oa.mapper.SysRolePermissionMapper;
import com.web.oa.mapper.SysUserRoleMapper;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysPermissionExample;
import com.web.oa.pojo.SysPermissionExample.Criteria;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysRolePermission;
import com.web.oa.pojo.SysRolePermissionExample;
import com.web.oa.pojo.SysUserRole;
import com.web.oa.service.SysService;

@Service
public class SysServiceImpl implements SysService {

	
	@Autowired
	private SysRoleMapper sysRoleMapper;
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	@Autowired
	private SysRolePermissionMapper  sysRolePermissionMapper;
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	
	
	@Override
	public List<MenuTree> loadMenuTree() {
		return sysPermissionMapperCustom.getMenuTree();
	}

	@Override
	public List<SysPermission> findPermissionListByUserId(String userid) throws Exception{
		return sysPermissionMapperCustom.findMenuAndPermissionByUserId(userid);
	}

	@Override
	public List<SysRole> findAllRoles() {
		return sysRoleMapper.selectByExample(null);
	}

	@Override
	public List<SysPermission> findMenuListByUserId(String userid) throws Exception {
		return sysPermissionMapperCustom.findMenuListByUserId(userid);
	}

	@Override
	public SysRole findRolesAndPermissionsByUserId(String userId) {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userId);
	}

	@Override
	public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
		// TODO Auto-generated method stub
		//添加角色
		sysRoleMapper.insert(role);
		//添加角色和权限关系表
		for (int i = 0; i < permissionIds.length; i++) {
			SysRolePermission permission = new SysRolePermission();
			String uuid = UUID.randomUUID().toString();
			permission.setId(uuid);
			permission.setSysRoleId(role.getId());
			permission.setSysPermissionId(permissionIds[i]+"");
			sysRolePermissionMapper.insert(permission);
		}
	}

	@Override
	public List<SysPermission> findAllMenus() {
		// TODO Auto-generated method stub
		SysPermissionExample example = new SysPermissionExample();
		Criteria criteria = example.createCriteria();
		criteria.andTypeEqualTo("menu");
		return sysPermissionMapper.selectByExample(example);
	}

	@Override
	public void addSysPermission(SysPermission permission) {
		// TODO Auto-generated method stub
		sysPermissionMapper.insert(permission);
		
	}

	@Override
	public List<SysPermission> findMenuAndPermissionByUserId(String userId) {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findMenuAndPermissionByUserId(userId);
	}

	@Override
	public List<MenuTree> getAllMenuAndPermision() {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.getAllMenuAndPermision();
	}

	@Override
	public List<SysPermission> findPermissionsByRoleId(String roleId) {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
	}

	@Override
	public void updateRoleAndPermissions(String roleId, int[] permissionIds) {
		// TODO Auto-generated method stub
		//先删除角色权限关系表中角色的权限关系
		SysRolePermissionExample example = new SysRolePermissionExample();
		com.web.oa.pojo.SysRolePermissionExample.Criteria criteria = example.createCriteria();
		criteria.andSysRoleIdEqualTo(roleId);
		sysRolePermissionMapper.deleteByExample(example);
		//重新创建用户权限关系
		for (Integer i : permissionIds) {
			SysRolePermission permission = new SysRolePermission();
			String uuid = UUID.randomUUID().toString();
			permission.setId(uuid);
			permission.setSysRoleId(roleId);
			permission.setSysPermissionId(i.toString());
			
			sysRolePermissionMapper.insert(permission);
		}
	}

	@Override
	public List<SysRole> findRolesAndPermissions() {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findRoleAndPermissionList();
	}

	@Override
	public int saveUserRole(SysUserRole UserRole) {
		return sysUserRoleMapper.insert(UserRole);
	}

	@Override
	public List<SysUserRole> findUserRole() {
		return sysUserRoleMapper.selectByExample(null);
	}

	@Override
	public void deleteRole(String roleId) {
		// TODO Auto-generated method stub
		sysRoleMapper.deleteByPrimaryKey(roleId);
	}

	
}

package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 * 
 * @author lijian
 * @since 1.0.0
 */
public interface SysRoleService extends IService<SysRole> {
    
    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @return 角色对象
     */
    SysRole getByRoleCode(String roleCode);
    
    /**
     * 分页查询角色列表
     * 
     * @param page 分页参数
     * @param roleName 角色名称
     * @return 角色分页列表
     */
    Page<SysRole> pageRoles(Page<SysRole> page, String roleName);
    
    /**
     * 获取所有启用状态的角色
     * 
     * @return 角色列表
     */
    List<SysRole> getAllEnabledRoles();
    
    /**
     * 创建角色
     * 
     * @param role 角色信息
     * @return 创建后的角色
     */
    SysRole createRole(SysRole role);
    
    /**
     * 更新角色
     * 
     * @param role 角色信息
     * @return 更新后的角色
     */
    SysRole updateRole(SysRole role);
    
    /**
     * 启用/禁用角色
     * 
     * @param roleId 角色ID
     * @param status 状态(0-禁用 1-启用)
     * @return 是否成功
     */
    boolean updateRoleStatus(Long roleId, Integer status);
    
    /**
     * 根据用户ID查询用户角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);
} 
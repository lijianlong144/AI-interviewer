package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.SysRole;
import com.lijian.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author lijian
 * @since 1.0.0
 */
public interface SysUserService extends IService<SysUser> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    SysUser getByUsername(String username);
    
    /**
     * 获取用户详情，包括角色信息
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    SysUser getUserDetail(Long userId);
    
    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @param roleIds 角色ID列表，为空时分配默认角色
     * @return 注册成功的用户
     */
    SysUser register(SysUser user, List<Long> roleIds);
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户（包含角色信息）
     */
    SysUser login(String username, String password);
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新后的用户
     */
    SysUser updateUserInfo(SysUser user);
    
    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 分页查询用户列表
     * 
     * @param page 分页参数
     * @param username 用户名
     * @param realName 真实姓名
     * @param roleId 角色ID
     * @return 用户分页列表
     */
    Page<SysUser> pageUsers(Page<SysUser> page, String username, String realName, Long roleId);
    
    /**
     * 根据角色ID查询用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    List<SysUser> getUsersByRoleId(Long roleId);
    
    /**
     * 启用/禁用用户
     * 
     * @param userId 用户ID
     * @param status 状态(0-禁用 1-启用)
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 分配用户角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getUserRoles(Long userId);
}

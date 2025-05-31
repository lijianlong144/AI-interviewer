package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.SysRole;
import com.lijian.entity.SysUser;
import com.lijian.entity.SysUserRole;
import com.lijian.enums.StatusEnum;
import com.lijian.enums.UserRoleEnum;
import com.lijian.exception.BusinessException;
import com.lijian.mapper.SysRoleMapper;
import com.lijian.mapper.SysUserMapper;
import com.lijian.mapper.SysUserRoleMapper;
import com.lijian.service.SysRoleService;
import com.lijian.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author lijian
 * @since 1.0.0
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysRoleService roleService;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        return this.getOne(queryWrapper);
    }
    
    /**
     * 获取用户详情，包括角色信息
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @Override
    public SysUser getUserDetail(Long userId) {
        // 查询用户基本信息
        SysUser user = this.getById(userId);
        if (user == null) {
            return null;
        }
        
        // 查询用户角色
        List<SysRole> roles = getUserRoles(userId);
        
        // 不返回密码
        user.setPassword(null);
        
        return user;
    }
    
    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @param roleIds 角色ID列表，为空时分配默认角色
     * @return 注册成功的用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser register(SysUser user, List<Long> roleIds) {
        // 检查用户名是否已存在
        SysUser existUser = this.getByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getEmail, user.getEmail());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        user.setStatus(StatusEnum.ENABLED.getCode());
        
        // 保存用户
        this.save(user);
        
        // 分配角色
        if (CollectionUtils.isEmpty(roleIds)) {
            // 如果没有指定角色，默认分配候选人角色
            SysRole defaultRole = roleService.getByRoleCode(UserRoleEnum.CANDIDATE.getCode());
            if (defaultRole != null) {
                roleIds = Collections.singletonList(defaultRole.getId());
            }
        }
        
        if (!CollectionUtils.isEmpty(roleIds)) {
            assignRoles(user.getId(), roleIds);
        }
        
        return user;
    }
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户（包含角色信息）
     */
    @Override
    public SysUser login(String username, String password) {
        // 查询用户
        SysUser user = this.getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (!StatusEnum.ENABLED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
        
        // 查询用户角色
        List<SysRole> roles = getUserRoles(user.getId());
        
        // 不返回密码
        user.setPassword(null);
        
        return user;
    }
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新后的用户
     */
    @Override
    public SysUser updateUserInfo(SysUser user) {
        // 检查用户是否存在
        SysUser existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不允许修改用户名
        user.setUsername(null);
        
        // 不允许修改密码（密码修改有专门的接口）
        user.setPassword(null);
        
        // 更新用户信息
        this.updateById(user);
        
        // 返回更新后的用户
        return getUserDetail(user.getId());
    }
    
    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
    
    /**
     * 分页查询用户列表
     * 
     * @param page 分页参数
     * @param username 用户名
     * @param realName 真实姓名
     * @param roleId 角色ID
     * @return 用户分页列表
     */
    @Override
    public Page<SysUser> pageUsers(Page<SysUser> page, String username, String realName, Long roleId) {
        // 如果指定了角色ID，先查询该角色下的用户ID
        List<Long> userIds = null;
        if (roleId != null) {
            LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.eq(SysUserRole::getRoleId, roleId);
            List<SysUserRole> userRoles = userRoleMapper.selectList(urWrapper);
            if (CollectionUtils.isEmpty(userRoles)) {
                return new Page<>(page.getCurrent(), page.getSize());
            }
            userIds = userRoles.stream().map(SysUserRole::getUserId).collect(Collectors.toList());
        }
        
        // 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        
        if (StringUtils.hasText(realName)) {
            queryWrapper.like(SysUser::getRealName, realName);
        }
        
        if (userIds != null) {
            queryWrapper.in(SysUser::getId, userIds);
        }
        
        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        // 查询并返回结果
        Page<SysUser> userPage = this.page(page, queryWrapper);
        
        // 查询用户角色并设置
        userPage.getRecords().forEach(user -> {
            // 不返回密码
            user.setPassword(null);
        });
        
        return userPage;
    }
    
    /**
     * 根据角色ID查询用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Override
    public List<SysUser> getUsersByRoleId(Long roleId) {
        // 查询角色下的用户ID
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getRoleId, roleId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(urWrapper);
        
        if (CollectionUtils.isEmpty(userRoles)) {
            return new ArrayList<>();
        }
        
        // 获取用户ID列表
        List<Long> userIds = userRoles.stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());
        
        // 查询用户信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUser::getId, userIds);
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLED.getCode());
        
        List<SysUser> users = this.list(queryWrapper);
        
        // 不返回密码
        users.forEach(user -> user.setPassword(null));
        
        return users;
    }
    
    /**
     * 启用/禁用用户
     * 
     * @param userId 用户ID
     * @param status 状态(0-禁用 1-启用)
     * @return 是否成功
     */
    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        // 检查用户是否存在
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新状态
        user.setStatus(status);
        return this.updateById(user);
    }
    
    /**
     * 分配用户角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        // 检查用户是否存在
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 删除原有角色
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(queryWrapper);
        
        // 如果角色ID列表为空，直接返回成功
        if (CollectionUtils.isEmpty(roleIds)) {
            return true;
        }
        
        // 分配新角色
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        
        return true;
    }
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> getUserRoles(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(queryWrapper);
        
        if (CollectionUtils.isEmpty(userRoles)) {
            return new ArrayList<>();
        }
        
        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 查询角色信息
        return roleMapper.selectBatchIds(roleIds);
    }
} 
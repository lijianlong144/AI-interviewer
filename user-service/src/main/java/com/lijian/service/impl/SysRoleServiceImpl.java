package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.SysRole;
import com.lijian.entity.SysUserRole;
import com.lijian.enums.StatusEnum;
import com.lijian.exception.BusinessException;
import com.lijian.mapper.SysRoleMapper;
import com.lijian.mapper.SysUserRoleMapper;
import com.lijian.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 
 * @author lijian
 * @since 1.0.0
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @return 角色对象
     */
    @Override
    public SysRole getByRoleCode(String roleCode) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getRoleCode, roleCode);
        return this.getOne(queryWrapper);
    }
    
    /**
     * 分页查询角色列表
     * 
     * @param page 分页参数
     * @param roleName 角色名称
     * @return 角色分页列表
     */
    @Override
    public Page<SysRole> pageRoles(Page<SysRole> page, String roleName) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(roleName)) {
            queryWrapper.like(SysRole::getRoleName, roleName);
        }
        
        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(SysRole::getCreateTime);
        
        return this.page(page, queryWrapper);
    }
    
    /**
     * 获取所有启用状态的角色
     * 
     * @return 角色列表
     */
    @Override
    public List<SysRole> getAllEnabledRoles() {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getStatus, StatusEnum.ENABLED.getCode());
        return this.list(queryWrapper);
    }
    
    /**
     * 创建角色
     * 
     * @param role 角色信息
     * @return 创建后的角色
     */
    @Override
    public SysRole createRole(SysRole role) {
        // 检查角色编码是否已存在
        SysRole existRole = this.getByRoleCode(role.getRoleCode());
        if (existRole != null) {
            throw new BusinessException("角色编码已存在");
        }
        
        // 设置默认状态
        role.setStatus(StatusEnum.ENABLED.getCode());
        
        // 保存角色
        this.save(role);
        
        return role;
    }
    
    /**
     * 更新角色
     * 
     * @param role 角色信息
     * @return 更新后的角色
     */
    @Override
    public SysRole updateRole(SysRole role) {
        // 检查角色是否存在
        SysRole existRole = this.getById(role.getId());
        if (existRole == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 检查角色编码是否已存在（排除自身）
        if (StringUtils.hasText(role.getRoleCode()) && !role.getRoleCode().equals(existRole.getRoleCode())) {
            LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysRole::getRoleCode, role.getRoleCode());
            queryWrapper.ne(SysRole::getId, role.getId());
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException("角色编码已存在");
            }
        }
        
        // 更新角色
        this.updateById(role);
        
        return this.getById(role.getId());
    }
    
    /**
     * 启用/禁用角色
     * 
     * @param roleId 角色ID
     * @param status 状态(0-禁用 1-启用)
     * @return 是否成功
     */
    @Override
    public boolean updateRoleStatus(Long roleId, Integer status) {
        // 检查角色是否存在
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 更新状态
        role.setStatus(status);
        return this.updateById(role);
    }
    
    /**
     * 根据用户ID查询用户角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(queryWrapper);
        
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 查询角色信息
        return this.listByIds(roleIds);
    }
} 
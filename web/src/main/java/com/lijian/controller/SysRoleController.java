package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.SysRole;
import com.lijian.result.Result;
import com.lijian.service.SysRoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/role")
@Validated
public class SysRoleController {
    
    @Autowired
    private SysRoleService roleService;
    
    /**
     * 创建角色
     */
    @PostMapping
    public Result<SysRole> createRole(@Valid @RequestBody SysRole role) {
        SysRole createdRole = roleService.createRole(role);
        return Result.success(createdRole);
    }
    
    /**
     * 更新角色
     */
    @PutMapping
    public Result<SysRole> updateRole(@Valid @RequestBody SysRole role) {
        SysRole updatedRole = roleService.updateRole(role);
        return Result.success(updatedRole);
    }
    
    /**
     * 获取角色信息
     */
    @GetMapping("/{id}")
    public Result<SysRole> getRoleInfo(@PathVariable("id") Long id) {
        SysRole role = roleService.getById(id);
        return Result.success(role);
    }
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public Result<Page<SysRole>> pageRoles(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String roleName) {
        Page<SysRole> page = new Page<>(current, size);
        Page<SysRole> rolePage = roleService.pageRoles(page, roleName);
        return Result.success(rolePage);
    }
    
    /**
     * 获取所有启用状态的角色
     */
    @GetMapping("/enabled")
    public Result<List<SysRole>> getAllEnabledRoles() {
        List<SysRole> roles = roleService.getAllEnabledRoles();
        return Result.success(roles);
    }
    
    /**
     * 启用/禁用角色
     */
    @PostMapping("/status")
    public Result<Boolean> updateRoleStatus(@RequestParam @NotNull(message = "角色ID不能为空") Long roleId,
                                          @RequestParam @NotNull(message = "状态不能为空") Integer status) {
        boolean result = roleService.updateRoleStatus(roleId, status);
        return Result.success(result);
    }
    
    /**
     * 根据用户ID查询用户角色
     */
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getRolesByUserId(@PathVariable("userId") Long userId) {
        List<SysRole> roles = roleService.getRolesByUserId(userId);
        return Result.success(roles);
    }
} 
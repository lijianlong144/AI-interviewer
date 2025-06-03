package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.SysRole;
import com.lijian.entity.SysUser;
import com.lijian.result.Result;
import com.lijian.service.SysUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class SysUserController {
    
    @Autowired
    private SysUserService userService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<SysUser> register(@Valid @RequestBody SysUser user) {
        SysUser registeredUser = userService.register(user, null);
        return Result.success(registeredUser);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<SysUser> login(@RequestParam @NotBlank(message = "用户名不能为空") String username,
                                @RequestParam @NotBlank(message = "密码不能为空") String password) {
        SysUser user = userService.login(username, password);
        return Result.success(user);
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUserInfo(@PathVariable("id") Long id) {
        SysUser user = userService.getUserDetail(id);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping
    public Result<SysUser> updateUserInfo(@Valid @RequestBody SysUser user) {
        SysUser updatedUser = userService.updateUserInfo(user);
        return Result.success(updatedUser);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestParam(value ="userId" ) @NotNull(message = "用户ID不能为空") Long userId,
                                        @RequestParam(value ="oldPassword" ) @NotBlank(message = "原密码不能为空") String oldPassword,
                                        @RequestParam(value ="newPassword" ) @NotBlank(message = "新密码不能为空") String newPassword) {
        boolean result = userService.changePassword(userId, oldPassword, newPassword);
        return Result.success(result);
    }
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<Page<SysUser>> pageUsers(@RequestParam(value = "current", defaultValue = "1") long current,
                                         @RequestParam(value = "size",defaultValue = "10") long size,
                                         @RequestParam(value = "username",required = false) String username,
                                         @RequestParam(value = "realName",required = false) String realName,
                                         @RequestParam(value = "roleId",required = false) Long roleId) {
        Page<SysUser> page = new Page<>(current, size);
        Page<SysUser> userPage = userService.pageUsers(page, username, realName, roleId);
        return Result.success(userPage);
    }
    
    /**
     * 启用/禁用用户
     */
    @PostMapping("/status")
    public Result<Boolean> updateUserStatus(@RequestParam(value = "id") @NotNull(message = "用户ID不能为空") Long id,
                                          @RequestParam(value = "status") @NotNull(message = "状态不能为空") Integer status) {
        boolean result = userService.updateUserStatus(id, status);
        return Result.success(result);
    }
    
    /**
     * 分配用户角色
     */
    @PostMapping("/assign-roles")
    public Result<Boolean> assignRoles(@RequestParam(value = "userId") @NotNull(message = "用户ID不能为空") Long userId,
                                     @RequestParam(value = "roleIds") @NotNull(message = "角色ID列表不能为空") List<Long> roleIds) {
        boolean result = userService.assignRoles(userId, roleIds);
        return Result.success(result);
    }
    
    /**
     * 获取用户角色列表
     */
    @GetMapping("/{userId}/roles")
    public Result<List<SysRole>> getUserRoles(@PathVariable("userId") Long userId) {
        List<SysRole> roles = userService.getUserRoles(userId);
        return Result.success(roles);
    }
} 
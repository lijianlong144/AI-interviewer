package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lijian.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 * 
 * @author lijian
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    
    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;
} 
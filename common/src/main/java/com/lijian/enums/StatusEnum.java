package com.lijian.enums;

/**
 * 状态枚举
 * 
 * @author lijian
 * @since 1.0.0
 */
public enum StatusEnum {
    
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    
    /**
     * 启用
     */
    ENABLED(1, "启用");
    
    private final Integer code;
    
    private final String desc;
    
    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
} 
package com.lijian.enums;

/**
 * 用户角色枚举
 * 
 * @author lijian
 * @since 1.0.0
 */
public enum UserRoleEnum {
    
    /**
     * 面试官
     */
    INTERVIEWER("INTERVIEWER", "面试官"),
    
    /**
     * HR
     */
    HR("HR", "人力资源"),
    
    /**
     * 候选人
     */
    CANDIDATE("CANDIDATE", "候选人");
    
    private final String code;
    
    private final String desc;
    
    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
} 
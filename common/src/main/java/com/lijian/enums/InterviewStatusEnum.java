package com.lijian.enums;

/**
 * 面试状态枚举
 * 
 * @author lijian
 * @since 1.0.0
 */
public enum InterviewStatusEnum {
    
    /**
     * 待进行
     */
    PENDING("PENDING", "待进行"),
    
    /**
     * 进行中
     */
    ONGOING("ONGOING", "进行中"),
    
    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),
    
    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    
    private final String desc;
    
    InterviewStatusEnum(String code, String desc) {
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
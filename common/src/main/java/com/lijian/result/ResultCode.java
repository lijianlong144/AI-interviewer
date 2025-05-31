package com.lijian.result;

/**
 * 结果状态码
 * 
 * @author lijian
 * @since 1.0.0
 */
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 失败
     */
    FAILURE(500, "操作失败"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR(500, "服务器内部错误");
    
    private final Integer code;
    
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
} 
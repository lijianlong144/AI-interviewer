// common/src/main/java/com/lijian/common/exception/BusinessException.java
package com.lijian.exception;

import com.lijian.result.ResultCode;

/**
 * 业务异常
 * 
 * @author lijian
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;
    
    public BusinessException() {
        super();
    }
    
    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = ResultCode.FAILURE.getCode();
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
    
    public BusinessException(Throwable cause) {
        super(cause);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = ResultCode.FAILURE.getCode();
    }
    
    public Integer getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
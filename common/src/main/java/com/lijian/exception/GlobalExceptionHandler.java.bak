// common/src/main/java/com/lijian/common/exception/GlobalExceptionHandler.java
package com.lijian.exception;

import com.lijian.result.Result;
import com.lijian.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 * 
 * @author lijian
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage());

        return Result.failure(e.getCode(), e.getMessage());
    }
    
    /**
     * 参数校验异常处理（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(", ");
        }
        String msg = sb.toString();
        if (sb.length() > 0) {
            msg = msg.substring(0, msg.length() - 2);
        }
        log.error("参数校验异常：{}", msg);
        return Result.failure(ResultCode.PARAM_ERROR.getCode(), msg);
    }
    
    /**
     * 参数校验异常处理（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            sb.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append(", ");
        }
        String msg = sb.toString();
        if (sb.length() > 0) {
            msg = msg.substring(0, msg.length() - 2);
        }
        log.error("参数校验异常：{}", msg);
        return Result.failure(ResultCode.PARAM_ERROR.getCode(), msg);
    }
    
    /**
     * 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(", ");
        }
        String msg = sb.toString();
        if (sb.length() > 0) {
            msg = msg.substring(0, msg.length() - 2);
        }
        log.error("参数绑定异常：{}", msg);
        return Result.failure(ResultCode.PARAM_ERROR.getCode(), msg);
    }
    
    /**
     * 其他异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failure(ResultCode.INTERNAL_ERROR.getCode(), "系统异常，请联系管理员");
    }
    
}
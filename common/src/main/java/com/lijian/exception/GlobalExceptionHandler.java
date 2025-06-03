package com.lijian.exception;

import com.lijian.result.Result;
import com.lijian.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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
 * 简化版全局异常处理器 - 开发环境专用
 * 直接显示所有错误的详细信息，方便开发调试
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
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.failure(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("参数校验失败：");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append("[").append(fieldError.getField()).append("]")
                    .append(fieldError.getDefaultMessage()).append("; ");
        }
        String msg = sb.toString();
        log.error("参数校验异常：{}", msg);
        return Result.failure(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 参数校验异常处理（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder sb = new StringBuilder("参数校验失败：");
        for (ConstraintViolation<?> violation : violations) {
            sb.append("[").append(violation.getPropertyPath()).append("]")
                    .append(violation.getMessage()).append("; ");
        }
        String msg = sb.toString();
        log.error("参数校验异常：{}", msg);
        return Result.failure(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        StringBuilder sb = new StringBuilder("参数绑定失败：");
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append("[").append(fieldError.getField()).append("]")
                    .append(fieldError.getDefaultMessage()).append("; ");
        }
        String msg = sb.toString();
        log.error("参数绑定异常：{}", msg);
        return Result.failure(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 数据库异常处理 - 开发环境直接显示详细错误
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        log.error("数据库操作异常", e);

        // 直接返回详细的错误信息，方便定位问题
        String detailMessage = String.format(
                "数据库错误：%s | 原因：%s",
                e.getClass().getSimpleName(),
                getRootCauseMessage(e)
        );

        return Result.failure(ResultCode.INTERNAL_ERROR.getCode(), detailMessage);
    }

    /**
     * 其他所有异常处理 - 开发环境显示详细信息
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);

        // 获取异常发生的位置
        StackTraceElement element = e.getStackTrace()[0];

        // 构建详细的错误信息
        String detailMessage = String.format(
                "系统异常[%s]: %s | 位置：%s.%s(%s:%d)",
                e.getClass().getSimpleName(),
                e.getMessage() != null ? e.getMessage() : "无错误信息",
                element.getClassName(),
                element.getMethodName(),
                element.getFileName(),
                element.getLineNumber()
        );

        return Result.failure(ResultCode.INTERNAL_ERROR.getCode(), detailMessage);
    }

    /**
     * 获取根异常消息
     */
    private String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.toString();
    }
}
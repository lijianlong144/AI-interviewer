package com.lijian.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用返回结果
 * 
 * @author lijian
 * @since 1.0.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功结果
     * 
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功结果
     * 
     * @param data 数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return success(ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功结果
     * 
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<T>()
                .setSuccess(true)
                .setCode(ResultCode.SUCCESS.getCode())
                .setMessage(message)
                .setData(data);
    }
    
    /**
     * 失败结果
     * 
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> failure() {
        return failure(ResultCode.FAILURE);
    }
    
    /**
     * 失败结果
     * 
     * @param resultCode 结果码
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> failure(ResultCode resultCode) {
        return failure(resultCode.getCode(), resultCode.getMessage());
    }
    
    /**
     * 失败结果
     * 
     * @param code 状态码
     * @param message 消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<T>()
                .setSuccess(false)
                .setCode(code)
                .setMessage(message);
    }
    
    /**
     * 失败结果
     * 
     * @param message 消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> failure(String message) {
        return failure(ResultCode.FAILURE.getCode(), message);
    }
    
    public static <T> Result<T> error() {
        return new Result<T>()
                .setSuccess(false)
                .setCode(ResultCode.FAILURE.getCode())
                .setMessage(ResultCode.FAILURE.getMessage());
    }

    public static <T> Result<T> error(String message) {
        return new Result<T>()
                .setSuccess(false)
                .setCode(ResultCode.FAILURE.getCode())
                .setMessage(message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>()
                .setSuccess(false)
                .setCode(code)
                .setMessage(message);
    }
    
    public Result(Integer code, String message, T data) {
        this.success = code.equals(ResultCode.SUCCESS.getCode());
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public Result<T> setSuccess(Boolean success) {
        this.success = success;
        return this;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public Result<T> setCode(Integer code) {
        this.code = code;
        return this;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }
    
    public T getData() {
        return data;
    }
    
    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
} 
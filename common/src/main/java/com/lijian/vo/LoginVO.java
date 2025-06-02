package com.lijian.vo;

import lombok.Data;

/**
 * 登录返回VO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class LoginVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token类型
     */
    private String tokenType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色
     */
    private String role;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;
}
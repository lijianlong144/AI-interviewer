// RegisterDTO.java
package com.lijian.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册DTO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名必须是4-16位字母、数字、下划线或减号")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,20}$", message = "密码必须是6-20位字母、数字、下划线或减号")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 角色，默认为CANDIDATE
     */
    private String role;
}

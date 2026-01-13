package org.example.tlbglxt.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户登录请求DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class UserLoginRequest {

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 用户类型（0-普通用户，1-医生）
     */
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    /**
     * 验证码（暂时不使用）
     */
    // private String captcha;

    /**
     * 验证码key（暂时不使用）
     */
    // private String captchaKey;
} 
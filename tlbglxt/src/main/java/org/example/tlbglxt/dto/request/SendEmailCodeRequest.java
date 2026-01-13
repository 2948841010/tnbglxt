package org.example.tlbglxt.dto.request;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送邮箱验证码请求DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class SendEmailCodeRequest {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 验证码类型（register-注册，reset-重置密码）
     */
    @NotBlank(message = "验证码类型不能为空")
    private String type;
} 
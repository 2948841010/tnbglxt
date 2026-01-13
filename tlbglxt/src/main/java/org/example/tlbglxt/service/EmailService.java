package org.example.tlbglxt.service;

/**
 * 邮箱服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     *
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @param type 验证码类型（register-注册，reset-重置密码）
     * @return 发送结果
     */
    boolean sendVerificationCode(String toEmail, String code, String type);

    /**
     * 发送普通邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果
     */
    boolean sendSimpleEmail(String toEmail, String subject, String content);

    /**
     * 发送HTML邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML邮件内容
     * @return 发送结果
     */
    boolean sendHtmlEmail(String toEmail, String subject, String htmlContent);
} 
package org.example.tlbglxt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮箱服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:2948841010@qq.com}")
    private String fromEmail;

    @Override
    public boolean sendVerificationCode(String toEmail, String code, String type) {
        log.info("发送验证码邮件，收件人：{}，验证码：{}，类型：{}", toEmail, code, type);

        try {
            String subject = getEmailSubject(type);
            String content = buildVerificationEmailContent(code, type);

            return sendHtmlEmail(toEmail, subject, content);

        } catch (Exception e) {
            log.error("发送验证码邮件失败，收件人：{}", toEmail, e);
            return false;
        }
    }

    @Override
    public boolean sendSimpleEmail(String toEmail, String subject, String content) {
        log.info("发送普通邮件，收件人：{}，主题：{}", toEmail, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);
            log.info("普通邮件发送成功，收件人：{}", toEmail);
            return true;

        } catch (Exception e) {
            log.error("发送普通邮件失败，收件人：{}", toEmail, e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        log.info("发送HTML邮件，收件人：{}，主题：{}", toEmail, subject);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("HTML邮件发送成功，收件人：{}", toEmail);
            return true;

        } catch (Exception e) {
            log.error("发送HTML邮件失败，收件人：{}", toEmail, e);
            return false;
        }
    }

    // ============================== 私有方法 ==============================

    /**
     * 获取邮件主题
     */
    private String getEmailSubject(String type) {
        switch (type) {
            case "register":
                return "【糖尿病智能管理系统】注册验证码";
            case "reset":
                return "【糖尿病智能管理系统】密码重置验证码";
            default:
                return "【糖尿病智能管理系统】验证码";
        }
    }

    /**
     * 构建验证码邮件内容
     */
    private String buildVerificationEmailContent(String code, String type) {
        String action = "register".equals(type) ? "注册" : "密码重置";
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <title>验证码邮件</title>" +
                "</head>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "        <div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "            <h1 style=\"color: white; margin: 0; font-size: 28px;\">糖尿病智能管理系统</h1>" +
                "            <p style=\"color: white; margin: 10px 0 0 0; font-size: 16px;\">您的健康管理专家</p>" +
                "        </div>" +
                "        <div style=\"background: #f8f9fa; padding: 40px; border-radius: 0 0 10px 10px; border: 1px solid #e9ecef;\">" +
                "            <h2 style=\"color: #495057; margin-bottom: 20px;\">" + action + "验证码</h2>" +
                "            <p style=\"font-size: 16px; margin-bottom: 30px;\">您好！您正在进行" + action + "操作，您的验证码是：</p>" +
                "            <div style=\"background: #fff; padding: 20px; border-radius: 8px; text-align: center; border: 2px dashed #667eea; margin: 30px 0;\">" +
                "                <span style=\"font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px;\">" + code + "</span>" +
                "            </div>" +
                "            <div style=\"background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;\">" +
                "                <p style=\"margin: 0; color: #856404;\">⚠️ <strong>重要提示：</strong></p>" +
                "                <ul style=\"margin: 10px 0 0 20px; color: #856404;\">" +
                "                    <li>验证码有效期为5分钟，请及时使用</li>" +
                "                    <li>如非本人操作，请忽略此邮件</li>" +
                "                    <li>请勿将验证码泄露给他人</li>" +
                "                </ul>" +
                "            </div>" +
                "            <hr style=\"border: none; border-top: 1px solid #dee2e6; margin: 30px 0;\">" +
                "            <div style=\"text-align: center; color: #6c757d; font-size: 14px;\">" +
                "                <p>这是一封自动发送的邮件，请勿回复。</p>" +
                "                <p>如有疑问，请联系客服：<a href=\"mailto:support@tlbglxt.com\" style=\"color: #667eea;\">support@tlbglxt.com</a></p>" +
                "                <p style=\"margin-top: 20px;\">&copy; 2024 糖尿病智能管理系统. All rights reserved.</p>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
} 
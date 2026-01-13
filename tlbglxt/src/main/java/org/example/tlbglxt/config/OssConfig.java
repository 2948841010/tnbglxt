package org.example.tlbglxt.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 阿里云OSS配置类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {

    /**
     * OSS访问端点
     */
    private String endpoint;

    /**
     * OSS存储桶名称
     */
    private String bucketName;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 最大文件大小
     */
    private String maxFileSize;

    /**
     * 允许的文件扩展名
     */
    private String allowedExtensions;

    /**
     * OSS基础URL
     */
    private String baseUrl;

    /**
     * 创建OSS客户端Bean
     *
     * @return OSS客户端
     */
    @Bean
    public OSS ossClient() {
        // 验证必要的配置参数
        if (!StringUtils.hasText(endpoint) || 
            !StringUtils.hasText(accessKeyId) || 
            !StringUtils.hasText(accessKeySecret)) {
            throw new IllegalArgumentException("OSS配置参数不完整，请检查配置文件");
        }

        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 获取最大文件大小（字节）
     *
     * @return 最大文件大小
     */
    public long getMaxFileSizeBytes() {
        if (!StringUtils.hasText(maxFileSize)) {
            return 10 * 1024 * 1024; // 默认10MB
        }

        String size = maxFileSize.toLowerCase();
        if (size.endsWith("kb")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
        } else if (size.endsWith("mb")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
        } else if (size.endsWith("gb")) {
            return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(size);
        }
    }

    /**
     * 获取允许的文件扩展名数组
     *
     * @return 扩展名数组
     */
    public String[] getAllowedExtensionsArray() {
        if (!StringUtils.hasText(allowedExtensions)) {
            return new String[]{"jpg", "jpeg", "png", "gif"};
        }
        return allowedExtensions.split(",");
    }

    /**
     * 检查文件扩展名是否被允许
     *
     * @param extension 文件扩展名
     * @return 是否允许
     */
    public boolean isAllowedExtension(String extension) {
        if (!StringUtils.hasText(extension)) {
            return false;
        }

        String[] allowed = getAllowedExtensionsArray();
        String ext = extension.toLowerCase();
        
        for (String allowedExt : allowed) {
            if (allowedExt.trim().equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }
} 
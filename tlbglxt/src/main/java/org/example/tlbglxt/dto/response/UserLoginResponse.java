package org.example.tlbglxt.dto.response;

import lombok.Data;
import org.example.tlbglxt.entity.User;

import java.util.Map;

/**
 * 用户登录响应DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class UserLoginResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private User userInfo;

    /**
     * 额外信息（如权限信息等）
     */
    private Map<String, Object> additionalInfo;
} 
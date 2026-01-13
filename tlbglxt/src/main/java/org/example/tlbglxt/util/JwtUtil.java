package org.example.tlbglxt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT令牌
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForJwtTokenGenerationAndValidation}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration; // 默认24小时（毫秒）

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration; // 默认7天（毫秒）

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成访问令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param claims   额外的声明
     * @return JWT令牌
     */
    public String generateAccessToken(String username, Long userId, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey());

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.compact();
    }

    /**
     * 生成访问令牌（简化版）
     */
    public String generateAccessToken(String username, Long userId) {
        return generateAccessToken(username, userId, null);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析JWT令牌
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }

    /**
     * 检查令牌是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证令牌
     */
    public Boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return (username.equals(tokenUsername) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证令牌（不检查用户名）
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为刷新令牌
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取剩余过期时间（毫秒）
     */
    public Long getRemainingTime(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            return expirationDate.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 获取令牌类型
     */
    public String getTokenType(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return "access";
        }
    }

    /**
     * 从令牌中获取指定的声明值
     *
     * @param token 令牌
     * @param claimName 声明名称
     * @return 声明值
     */
    public String getClaimFromToken(String token, String claimName) {
        try {
            Claims claims = parseToken(token);
            return claims.get(claimName, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // 添加专门获取userType的方法
    public String getUserTypeFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                // 尝试多种可能的key
                Object userType = claims.get("userType");
                if (userType == null) {
                    userType = claims.get("user_type");
                }
                if (userType == null) {
                    userType = claims.get("type");
                }
                return userType != null ? userType.toString() : null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // 专门为登录生成包含用户类型的token
    public String generateAccessTokenWithUserType(String username, Long userId, Integer userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType); // 确保包含userType
        claims.put("user_type", userType); // 备用key
        claims.put("type", "access");
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
} 
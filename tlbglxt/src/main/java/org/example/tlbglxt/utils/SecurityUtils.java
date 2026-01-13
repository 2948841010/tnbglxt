package org.example.tlbglxt.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前用户ID
     * 这里从请求头中获取用户ID，您可以根据实际的认证机制调整
     *
     * @return 当前用户ID
     */
    public static Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 尝试从请求头中获取用户ID
                String userIdHeader = request.getHeader("X-User-Id");
                if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
                    return Long.valueOf(userIdHeader);
                }
                
                // 尝试从请求参数中获取用户ID
                String userIdParam = request.getParameter("userId");
                if (userIdParam != null && !userIdParam.trim().isEmpty()) {
                    return Long.valueOf(userIdParam);
                }
                
                // 如果都获取不到，返回默认用户ID（生产环境中应该抛出异常）
                log.warn("无法获取当前用户ID，使用默认用户ID: 1");
                return 1L;
            }
            
            log.warn("无法获取请求上下文，使用默认用户ID: 1");
            return 1L;
            
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return 1L; // 返回默认用户ID
        }
    }

    /**
     * 获取当前用户名（如果需要的话）
     *
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                String username = request.getHeader("X-Username");
                if (username != null && !username.trim().isEmpty()) {
                    return username;
                }
                
                return "anonymous";
            }
            
            return "anonymous";
            
        } catch (Exception e) {
            log.error("获取当前用户名失败", e);
            return "anonymous";
        }
    }
} 
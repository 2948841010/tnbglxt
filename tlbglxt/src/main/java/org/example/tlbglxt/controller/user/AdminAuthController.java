package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.UserLoginRequest;
import org.example.tlbglxt.dto.response.UserLoginResponse;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.service.PermissionService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.example.tlbglxt.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器
 * 专门处理管理员用户的认证相关操作
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "管理员认证", description = "管理员认证相关接口")
@RestController
@RequestMapping("/api/v1/admin/auth")
@Validated
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 管理员登录
     * 只允许 userType = 2 的管理员用户登录
     */
    @Operation(summary = "管理员登录", description = "管理员用户登录，返回访问令牌和用户信息")
    @PostMapping("/login")
    public Result<UserLoginResponse> adminLogin(@RequestBody @Valid UserLoginRequest request) {
        log.info("管理员登录请求，用户名：{}", request.getUsername());
        
        try {
            // 调用用户服务进行登录验证
            Result<UserLoginResponse> loginResult = userService.login(request);
            
            if (!loginResult.isSuccess()) {
                return loginResult;
            }
            
            UserLoginResponse loginResponse = loginResult.getData();
            User user = loginResponse.getUserInfo();
            
            // 验证是否为管理员用户
            if (user.getUserType() != 2) {
                log.warn("非管理员用户尝试登录管理端，用户名：{}，用户类型：{}", request.getUsername(), user.getUserType());
                return Result.error("您不是管理员，无法访问管理系统");
            }
            
            // 获取用户权限信息
            try {
                Result<?> permissionResult = permissionService.getUserPermissionInfo(user.getId());
                if (permissionResult.isSuccess()) {
                    // 将权限信息添加到响应中
                    Map<String, Object> additionalInfo = new HashMap<>();
                    additionalInfo.put("permissions", permissionResult.getData());
                    loginResponse.setAdditionalInfo(additionalInfo);
                }
            } catch (Exception e) {
                log.warn("获取管理员权限信息失败，用户ID：{}，错误：{}", user.getId(), e.getMessage());
                // 权限获取失败不影响登录，可以在后续请求中重新获取
            }
            
            log.info("管理员登录成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
            return Result.success(loginResponse);
            
        } catch (Exception e) {
            log.error("管理员登录失败，用户名：{}，错误：{}", request.getUsername(), e.getMessage(), e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 管理员登出
     */
    @Operation(summary = "管理员登出", description = "管理员用户登出")
    @PostMapping("/logout")
    public Result<Void> adminLogout(@RequestHeader("Authorization") String token) {
        log.info("管理员登出请求");
        
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(jwtToken);
                
                if (userId != null) {
                    // 清理Redis中的用户在线状态
                    try {
                        String onlineKey = "user:online:" + userId;
                        redisUtil.del(onlineKey);
                        log.info("清理管理员在线状态，用户ID：{}", userId);
                    } catch (Exception redisException) {
                        log.warn("清理Redis在线状态失败，用户ID：{}，错误：{}", userId, redisException.getMessage());
                    }
                    
                    // 获取用户信息验证是否为管理员
                    Result<User> userResult = userService.getUserById(userId);
                    if (userResult.isSuccess() && userResult.getData() != null) {
                        User user = userResult.getData();
                        if (user.getUserType() == 2) {
                            log.info("管理员登出成功，用户ID：{}，用户名：{}", userId, user.getUsername());
                        }
                    }
                }
            }
            
            // TODO: 实现JWT令牌黑名单功能
            return Result.success("登出成功");
            
        } catch (Exception e) {
            log.error("管理员登出处理失败", e);
            // 即使处理失败，也返回成功，不影响用户登出
            return Result.success("登出成功");
        }
    }

    /**
     * 获取当前管理员信息
     */
    @Operation(summary = "获取当前管理员信息", description = "获取当前登录的管理员用户信息")
    @GetMapping("/current")
    public Result<User> getCurrentAdmin(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("无效的访问令牌");
            }
            
            String jwtToken = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);
            
            if (userId == null) {
                return Result.error("无效的访问令牌");
            }
            
            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            User user = userResult.getData();
            
            // 验证是否为管理员用户
            if (user.getUserType() != 2) {
                return Result.error("您不是管理员用户");
            }
            
            // 清除敏感信息
            user.setPassword(null);
            
            return Result.success(user);
            
        } catch (Exception e) {
            log.error("获取当前管理员信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 验证访问令牌
     */
    @Operation(summary = "验证访问令牌", description = "验证当前访问令牌是否有效")
    @GetMapping("/validate")
    public Result<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("无效的访问令牌");
            }
            
            String jwtToken = token.substring(7);
            
            // 验证令牌是否有效
            if (!jwtUtil.validateToken(jwtToken)) {
                return Result.error("访问令牌已过期或无效");
            }
            
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);
            String username = jwtUtil.getUsernameFromToken(jwtToken);
            
            // 验证用户是否为管理员
            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            User user = userResult.getData();
            if (user.getUserType() != 2) {
                return Result.error("您不是管理员用户");
            }
            
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("userId", userId);
            tokenInfo.put("username", username);
            tokenInfo.put("userType", user.getUserType());
            tokenInfo.put("valid", true);
            
            return Result.success(tokenInfo);
            
        } catch (Exception e) {
            log.error("验证访问令牌失败", e);
            return Result.error("令牌验证失败");
        }
    }

    /**
     * 刷新访问令牌
     * TODO: 实现刷新令牌功能
     */
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<UserLoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        log.info("刷新管理员访问令牌请求");
        
        // TODO: 实现刷新令牌逻辑
        // 1. 验证刷新令牌的有效性
        // 2. 检查用户是否为管理员
        // 3. 生成新的访问令牌和刷新令牌
        // 4. 返回新的令牌信息
        
        return Result.error("刷新令牌功能暂未实现");
    }
} 
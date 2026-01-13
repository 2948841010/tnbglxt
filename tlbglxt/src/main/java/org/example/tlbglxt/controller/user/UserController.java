package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.exception.BusinessException;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;

/**
 * 用户管理控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ============================== 用户查询 ==============================

    /**
     * 根据ID获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable @Min(1) Long id) {
        log.info("获取用户信息请求，用户ID：{}", id);
        return userService.getUserById(id);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        log.info("获取当前用户信息请求");
        
        try {
            // 1. 验证Authorization头格式
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未提供有效的授权令牌");
            }

            // 2. 提取JWT令牌（去掉"Bearer "前缀）
            String token = authorization.substring(7);
            
            // 3. 验证令牌有效性
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), "令牌已过期或无效");
            }

            // 4. 从令牌中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从令牌中获取用户信息");
            }

            // 5. 获取用户详细信息
            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST.getCode(), "用户不存在");
            }

            // 6. 返回用户信息（排除敏感数据）
            User user = userResult.getData();
            user.setPassword(null); // 不返回密码
            
            log.info("获取当前用户信息成功，用户ID：{}", userId);
            return Result.success("获取成功", user);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "获取用户信息失败");
        }
    }

    // ============================== 用户信息更新 ==============================

    /**
     * 更新用户个人信息
     */
    @Operation(summary = "更新用户个人信息", description = "更新当前用户的基础信息")
    @PutMapping("/profile")
    public Result<Void> updateUserProfile(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Validated User user) {
        log.info("更新用户个人信息请求");
        
        try {
            // 1. 验证Authorization头格式
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未提供有效的授权令牌");
            }

            // 2. 提取JWT令牌（去掉"Bearer "前缀）
            String token = authorization.substring(7);
            
            // 3. 验证令牌有效性
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), "令牌已过期或无效");
            }

            // 4. 从令牌中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从令牌中获取用户信息");
            }

            // 5. 设置用户ID（确保只能更新自己的信息）
            user.setId(userId);

            // 6. 更新用户信息
            Result<Void> result = userService.updateUser(userId, user);
            
            log.info("更新用户个人信息成功，用户ID：{}", userId);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户个人信息失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "更新用户信息失败");
        }
    }
} 
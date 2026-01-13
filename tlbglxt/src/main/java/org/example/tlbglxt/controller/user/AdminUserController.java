package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户管理控制器
 * 提供用户管理相关接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "管理员用户管理", description = "管理员用户管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/users")
@Validated
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持搜索和筛选")
    @GetMapping("/list")
    public Result<PageResult<User>> getUserList(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员查询用户列表，页码：{}，页面大小：{}，关键词：{}", page, size, keyword);
            
            PageResult<User> result = userService.getUserList(page, size, keyword, userType, status, startTime, endTime);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error("查询用户列表失败");
        }
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable @NotNull @Min(1) Long userId,
            @RequestParam @NotNull Integer status,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员更新用户状态，用户ID：{}，状态：{}", userId, status);
            
            return userService.updateUserStatus(userId, status);
            
        } catch (Exception e) {
            log.error("更新用户状态失败，用户ID：{}，状态：{}", userId, status, e);
            return Result.error("更新用户状态失败");
        }
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    @PutMapping("/{userId}")
    public Result<Void> updateUser(
            @PathVariable @NotNull @Min(1) Long userId,
            @RequestBody @Validated User user,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员更新用户信息，用户ID：{}", userId);
            
            return userService.updateUser(userId, user);
            
        } catch (Exception e) {
            log.error("更新用户信息失败，用户ID：{}", userId, e);
            return Result.error("更新用户信息失败");
        }
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "逻辑删除指定用户")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(
            @PathVariable @NotNull @Min(1) Long userId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员删除用户，用户ID：{}", userId);
            
            return userService.deleteUser(userId);
            
        } catch (Exception e) {
            log.error("删除用户失败，用户ID：{}", userId, e);
            return Result.error("删除用户失败");
        }
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "批量逻辑删除用户")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteUsers(
            @RequestBody List<Long> userIds,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员批量删除用户，用户ID列表：{}", userIds);
            
            return userService.batchDeleteUsers(userIds);
            
        } catch (Exception e) {
            log.error("批量删除用户失败，用户ID列表：{}", userIds, e);
            return Result.error("批量删除用户失败");
        }
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "获取指定用户的详细信息")
    @GetMapping("/{userId}")
    public Result<User> getUserDetail(
            @PathVariable @NotNull @Min(1) Long userId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员查询用户详情，用户ID：{}", userId);
            
            Result<User> result = userService.getUserById(userId);
            if (result.isSuccess() && result.getData() != null) {
                // 清除密码字段
                result.getData().setPassword(null);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("查询用户详情失败，用户ID：{}", userId, e);
            return Result.error("查询用户详情失败");
        }
    }

    /**
     * 验证是否为管理员
     */
    private boolean isAdmin(String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return false;
            }

            String token = authorization.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return false;
            }

            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return false;
            }

            User user = userResult.getData();
            return user.getUserType() != null && user.getUserType() == 2;

        } catch (Exception e) {
            log.error("验证管理员权限失败", e);
            return false;
        }
    }
} 
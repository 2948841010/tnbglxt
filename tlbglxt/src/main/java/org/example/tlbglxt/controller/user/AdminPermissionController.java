package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.response.UserPermissionInfo;
import org.example.tlbglxt.entity.Menu;
import org.example.tlbglxt.entity.Role;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.service.PermissionService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员权限管理控制器
 * 处理权限相关的管理接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "管理员权限管理", description = "管理员权限管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/permissions")
public class AdminPermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取用户权限信息
     */
    @Operation(summary = "获取用户权限信息", description = "获取指定用户的权限信息，包括菜单和角色")
    @GetMapping("/user/{userId}")
    public Result<UserPermissionInfo> getUserPermissionInfo(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            return permissionService.getUserPermissionInfo(userId);
            
        } catch (Exception e) {
            log.error("获取用户权限信息失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            return Result.error("获取用户权限信息失败");
        }
    }

    /**
     * 获取所有角色
     */
    @Operation(summary = "获取所有角色", description = "获取系统中所有有效的角色列表")
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            List<Role> roles = permissionService.getAllActiveRoles();
            return Result.success(roles);
            
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.error("获取角色列表失败");
        }
    }

    /**
     * 获取所有菜单权限
     */
    @Operation(summary = "获取所有菜单权限", description = "获取系统中所有有效的菜单权限")
    @GetMapping("/menus")
    public Result<List<Menu>> getAllMenus(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            List<Menu> menus = permissionService.getAllActiveMenus();
            return Result.success(menus);
            
        } catch (Exception e) {
            log.error("获取菜单权限失败", e);
            return Result.error("获取菜单权限失败");
        }
    }

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色", description = "创建新的系统角色")
    @PostMapping("/roles")
    public Result<Void> createRole(
            @RequestBody Role role,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            // TODO: 实现角色创建逻辑
            log.info("创建角色请求：{}", role.getRoleName());
            return Result.error("角色创建功能暂未实现");
            
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.error("创建角色失败");
        }
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/roles/{roleId}")
    public Result<Void> updateRole(
            @PathVariable Long roleId,
            @RequestBody Role role,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            // TODO: 实现角色更新逻辑
            log.info("更新角色请求，角色ID：{}，角色名：{}", roleId, role.getRoleName());
            return Result.error("角色更新功能暂未实现");
            
        } catch (Exception e) {
            log.error("更新角色失败，角色ID：{}", roleId, e);
            return Result.error("更新角色失败");
        }
    }

    /**
     * 分配角色权限
     */
    @Operation(summary = "分配角色权限", description = "为角色分配菜单权限")
    @PostMapping("/roles/{roleId}/permissions")
    public Result<Void> assignRolePermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> menuIds,
            @RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Long adminId = getAdminIdFromToken(token);
            boolean success = permissionService.assignMenusToRole(roleId, menuIds, adminId);
            
            if (success) {
                log.info("角色权限分配成功，角色ID：{}，菜单数量：{}", roleId, menuIds.size());
                return Result.success("权限分配成功");
            } else {
                return Result.error("权限分配失败");
            }
            
        } catch (Exception e) {
            log.error("分配角色权限失败，角色ID：{}", roleId, e);
            return Result.error("权限分配失败");
        }
    }

    // ========================= 私有方法 =========================

    /**
     * 验证是否为管理员用户
     */
    private boolean isAdmin(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return false;
            }
            
            String jwtToken = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);
            
            if (userId == null) {
                return false;
            }
            
            Result<User> userResult = userService.getUserById(userId);
            return userResult.isSuccess() && 
                   userResult.getData() != null && 
                   userResult.getData().getUserType() == 2;
                   
        } catch (Exception e) {
            log.warn("验证管理员权限失败", e);
            return false;
        }
    }

    /**
     * 从Token中获取管理员ID
     */
    private Long getAdminIdFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                return jwtUtil.getUserIdFromToken(jwtToken);
            }
        } catch (Exception e) {
            log.warn("从Token获取管理员ID失败", e);
        }
        return null;
    }
} 
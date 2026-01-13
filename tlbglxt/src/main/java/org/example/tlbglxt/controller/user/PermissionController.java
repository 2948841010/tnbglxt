package org.example.tlbglxt.controller.user;

import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.entity.Menu;
import org.example.tlbglxt.entity.Role;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.service.PermissionService;
import org.example.tlbglxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端权限控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/user/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户菜单权限
     *
     * @param userId 用户ID
     * @return 菜单树
     */
    @GetMapping("/menus")
    public Result<List<Menu>> getUserMenus(@RequestParam Long userId) {
        try {
            List<Menu> menuTree = permissionService.getUserMenus(userId);
            return Result.success(menuTree);
        } catch (Exception e) {
            return Result.error("获取用户菜单失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/roles")
    public Result<List<Role>> getUserRoles(@RequestParam Long userId) {
        try {
            List<Role> roles = permissionService.getUserRoles(userId);
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error("获取用户角色失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户权限信息（菜单 + 角色）
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserPermissionInfo(@RequestParam Long userId) {
        try {
            // 获取用户信息
            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            User user = userResult.getData();

            // 获取菜单权限
            List<Menu> menus = permissionService.getUserMenus(userId);
            
            // 获取角色信息
            List<Role> roles = permissionService.getUserRoles(userId);

            Map<String, Object> permissionInfo = new HashMap<>();
            permissionInfo.put("user", user);
            permissionInfo.put("menus", menus);
            permissionInfo.put("roles", roles);

            return Result.success(permissionInfo);
        } catch (Exception e) {
            return Result.error("获取用户权限信息失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否有指定菜单权限
     *
     * @param userId 用户ID
     * @param menuPath 菜单路径
     * @return 是否有权限
     */
    @GetMapping("/check-menu")
    public Result<Boolean> checkMenuPermission(@RequestParam Long userId, 
                                              @RequestParam String menuPath) {
        try {
            boolean hasPermission = permissionService.hasMenuPermission(userId, menuPath);
            return Result.success(hasPermission);
        } catch (Exception e) {
            return Result.error("检查菜单权限失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    @GetMapping("/check-role")
    public Result<Boolean> checkRole(@RequestParam Long userId, 
                                   @RequestParam String roleCode) {
        try {
            boolean hasRole = permissionService.hasRole(userId, roleCode);
            return Result.success(hasRole);
        } catch (Exception e) {
            return Result.error("检查用户角色失败：" + e.getMessage());
        }
    }
} 
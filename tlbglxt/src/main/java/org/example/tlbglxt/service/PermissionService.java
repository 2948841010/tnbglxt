package org.example.tlbglxt.service;

import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.response.UserPermissionInfo;
import org.example.tlbglxt.entity.Menu;
import org.example.tlbglxt.entity.Role;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 根据用户ID获取用户菜单权限
     *
     * @param userId 用户ID
     * @return 菜单列表（树形结构）
     */
    List<Menu> getUserMenus(Long userId);

    /**
     * 根据用户ID获取用户角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 检查用户是否有指定菜单权限
     *
     * @param userId 用户ID
     * @param menuPath 菜单路径或权限标识
     * @return 是否有权限
     */
    boolean hasMenuPermission(Long userId, String menuPath);

    /**
     * 检查用户是否有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds, Long operatorId);

    /**
     * 为角色分配菜单权限
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean assignMenusToRole(Long roleId, List<Long> menuIds, Long operatorId);

    /**
     * 构建菜单树
     *
     * @param menus 菜单列表
     * @return 菜单树
     */
    List<Menu> buildMenuTree(List<Menu> menus);

    /**
     * 获取所有有效角色
     *
     * @return 角色列表
     */
    List<Role> getAllActiveRoles();

    /**
     * 获取所有有效菜单
     *
     * @return 菜单列表
     */
    List<Menu> getAllActiveMenus();

    /**
     * 获取用户权限信息（包含菜单和角色）
     *
     * @param userId 用户ID
     * @return 用户权限信息
     */
    Result<UserPermissionInfo> getUserPermissionInfo(Long userId);
} 
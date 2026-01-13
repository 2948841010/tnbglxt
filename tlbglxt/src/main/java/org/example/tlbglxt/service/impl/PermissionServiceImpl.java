package org.example.tlbglxt.service.impl;

import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.response.UserPermissionInfo;
import org.example.tlbglxt.entity.Menu;
import org.example.tlbglxt.entity.Role;
import org.example.tlbglxt.entity.RoleMenu;
import org.example.tlbglxt.entity.UserRole;
import org.example.tlbglxt.mapper.MenuMapper;
import org.example.tlbglxt.mapper.RoleMapper;
import org.example.tlbglxt.mapper.RoleMenuMapper;
import org.example.tlbglxt.mapper.UserRoleMapper;
import org.example.tlbglxt.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<Menu> getUserMenus(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        // 获取用户的菜单权限
        List<Menu> userMenus = menuMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(userMenus)) {
            return new ArrayList<>();
        }

        // 构建菜单树
        return buildMenuTree(userMenus);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return roleMapper.selectByUserId(userId);
    }

    @Override
    public boolean hasMenuPermission(Long userId, String menuPath) {
        if (userId == null || menuPath == null || menuPath.trim().isEmpty()) {
            return false;
        }

        List<Menu> userMenus = menuMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(userMenus)) {
            return false;
        }

        // 检查路径或权限标识是否匹配
        return userMenus.stream()
                .anyMatch(menu -> menuPath.equals(menu.getPath()) 
                        || menuPath.equals(menu.getPermission()));
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null || roleCode.trim().isEmpty()) {
            return false;
        }

        List<Role> userRoles = roleMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return false;
        }

        return userRoles.stream()
                .anyMatch(role -> roleCode.equals(role.getRoleCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds, Long operatorId) {
        if (userId == null || operatorId == null) {
            return false;
        }

        try {
            // 先删除用户现有的角色关联
            userRoleMapper.deleteByUserId(userId);

            // 如果角色列表不为空，则插入新的角色关联
            if (!CollectionUtils.isEmpty(roleIds)) {
                List<UserRole> userRoles = roleIds.stream()
                        .map(roleId -> {
                            UserRole userRole = new UserRole();
                            userRole.setUserId(userId);
                            userRole.setRoleId(roleId);
                            userRole.setCreateBy(operatorId);
                            userRole.setCreateTime(LocalDateTime.now());
                            return userRole;
                        })
                        .collect(Collectors.toList());

                userRoleMapper.batchInsert(userRoles);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("分配用户角色失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenusToRole(Long roleId, List<Long> menuIds, Long operatorId) {
        if (roleId == null || operatorId == null) {
            return false;
        }

        try {
            // 先删除角色现有的菜单权限
            roleMenuMapper.deleteByRoleId(roleId);

            // 如果菜单列表不为空，则插入新的菜单权限
            if (!CollectionUtils.isEmpty(menuIds)) {
                List<RoleMenu> roleMenus = menuIds.stream()
                        .map(menuId -> {
                            RoleMenu roleMenu = new RoleMenu();
                            roleMenu.setRoleId(roleId);
                            roleMenu.setMenuId(menuId);
                            roleMenu.setCreateBy(operatorId);
                            roleMenu.setCreateTime(LocalDateTime.now());
                            return roleMenu;
                        })
                        .collect(Collectors.toList());

                roleMenuMapper.batchInsert(roleMenus);
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("分配角色菜单权限失败", e);
        }
    }

    @Override
    public List<Menu> buildMenuTree(List<Menu> menus) {
        if (CollectionUtils.isEmpty(menus)) {
            return new ArrayList<>();
        }

        // 按父ID分组
        Map<Long, List<Menu>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(
                        menu -> menu.getParentId() == null ? 0L : menu.getParentId()
                ));

        // 递归构建树形结构
        return buildMenuTreeRecursive(0L, menuMap);
    }

    /**
     * 递归构建菜单树
     *
     * @param parentId 父菜单ID
     * @param menuMap 菜单映射
     * @return 菜单列表
     */
    private List<Menu> buildMenuTreeRecursive(Long parentId, Map<Long, List<Menu>> menuMap) {
        List<Menu> children = menuMap.get(parentId);
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }

        // 排序并设置子菜单
        children.sort(Comparator.comparing(
                menu -> menu.getSortOrder() == null ? 0 : menu.getSortOrder()
        ));

        for (Menu menu : children) {
            List<Menu> subChildren = buildMenuTreeRecursive(menu.getId(), menuMap);
            menu.setChildren(subChildren);
        }

        return children;
    }

    @Override
    public List<Role> getAllActiveRoles() {
        return roleMapper.selectAllActive();
    }

    @Override
    public List<Menu> getAllActiveMenus() {
        return menuMapper.selectAllActive();
    }

    @Override
    public Result<UserPermissionInfo> getUserPermissionInfo(Long userId) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            UserPermissionInfo permissionInfo = new UserPermissionInfo();

            // 获取用户菜单权限
            List<Menu> menus = getUserMenus(userId);
            permissionInfo.setMenus(menus);

            // 获取用户角色
            List<Role> roles = getUserRoles(userId);
            permissionInfo.setRoles(roles);

            // 提取权限标识列表（从菜单中提取）
            List<String> permissions = new ArrayList<>();
            extractPermissions(menus, permissions);
            permissionInfo.setPermissions(permissions);

            // 提取角色编码列表
            List<String> roleCodes = roles.stream()
                    .map(Role::getRoleCode)
                    .filter(roleCode -> roleCode != null)
                    .collect(Collectors.toList());
            permissionInfo.setRoleCodes(roleCodes);

            return Result.success(permissionInfo);

        } catch (Exception e) {
            return Result.error("获取用户权限信息失败：" + e.getMessage());
        }
    }

    /**
     * 递归提取菜单权限标识
     *
     * @param menus 菜单列表
     * @param permissions 权限标识列表
     */
    private void extractPermissions(List<Menu> menus, List<String> permissions) {
        if (CollectionUtils.isEmpty(menus)) {
            return;
        }

        for (Menu menu : menus) {
            // 添加菜单权限标识
            if (menu.getPermission() != null && !menu.getPermission().trim().isEmpty()) {
                permissions.add(menu.getPermission());
            }

            // 递归处理子菜单
            if (!CollectionUtils.isEmpty(menu.getChildren())) {
                extractPermissions(menu.getChildren(), permissions);
            }
        }
    }
} 
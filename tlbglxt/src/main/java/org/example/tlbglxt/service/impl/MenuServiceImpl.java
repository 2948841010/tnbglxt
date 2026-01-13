package org.example.tlbglxt.service.impl;

import org.example.tlbglxt.entity.Menu;
import org.example.tlbglxt.mapper.MenuMapper;
import org.example.tlbglxt.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public Menu getMenuById(Long id) {
        if (id == null) {
            return null;
        }
        return menuMapper.selectById(id);
    }

    @Override
    public List<Menu> getAllActiveMenus() {
        return menuMapper.selectAllActive();
    }

    @Override
    public List<Menu> getMenusByParentId(Long parentId) {
        return menuMapper.selectByParentId(parentId);
    }

    @Override
    public List<Menu> getMenusByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return menuMapper.selectByUserId(userId);
    }

    @Override
    public List<Menu> getMenusByRoleId(Long roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        return menuMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Menu> getMenusByType(Integer menuType) {
        if (menuType == null) {
            return new ArrayList<>();
        }
        return menuMapper.selectByMenuType(menuType);
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
    public boolean createMenu(Menu menu, Long operatorId) {
        if (menu == null || !StringUtils.hasText(menu.getMenuName())) {
            return false;
        }

        // 检查菜单编码是否已存在
        if (StringUtils.hasText(menu.getMenuCode()) && 
            isMenuCodeExists(menu.getMenuCode(), null)) {
            throw new RuntimeException("菜单编码已存在：" + menu.getMenuCode());
        }

        // 检查路径是否已存在
        if (StringUtils.hasText(menu.getPath()) && 
            isPathExists(menu.getPath(), null)) {
            throw new RuntimeException("菜单路径已存在：" + menu.getPath());
        }

        menu.setCreateBy(operatorId);
        menu.setUpdateBy(operatorId);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menu.setIsDeleted(0);

        if (menu.getStatus() == null) {
            menu.setStatus(1); // 默认启用
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0); // 默认排序
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L); // 默认为顶级菜单
        }

        return menuMapper.insert(menu) > 0;
    }

    @Override
    public boolean updateMenu(Menu menu, Long operatorId) {
        if (menu == null || menu.getId() == null) {
            return false;
        }

        // 检查菜单编码是否已存在（排除当前记录）
        if (StringUtils.hasText(menu.getMenuCode()) && 
            isMenuCodeExists(menu.getMenuCode(), menu.getId())) {
            throw new RuntimeException("菜单编码已存在：" + menu.getMenuCode());
        }

        // 检查路径是否已存在（排除当前记录）
        if (StringUtils.hasText(menu.getPath()) && 
            isPathExists(menu.getPath(), menu.getId())) {
            throw new RuntimeException("菜单路径已存在：" + menu.getPath());
        }

        menu.setUpdateBy(operatorId);
        menu.setUpdateTime(LocalDateTime.now());

        return menuMapper.updateById(menu) > 0;
    }

    @Override
    public boolean deleteMenu(Long id, Long operatorId) {
        if (id == null) {
            return false;
        }

        // 检查是否有子菜单
        if (hasChildren(id)) {
            throw new RuntimeException("存在子菜单，无法删除");
        }

        return menuMapper.deleteById(id, operatorId) > 0;
    }

    @Override
    public boolean isMenuCodeExists(String menuCode, Long excludeId) {
        if (!StringUtils.hasText(menuCode)) {
            return false;
        }
        return menuMapper.existsByMenuCode(menuCode, excludeId);
    }

    @Override
    public boolean isPathExists(String path, Long excludeId) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return menuMapper.existsByPath(path, excludeId);
    }

    @Override
    public boolean hasChildren(Long parentId) {
        if (parentId == null) {
            return false;
        }
        return menuMapper.hasChildren(parentId);
    }
} 
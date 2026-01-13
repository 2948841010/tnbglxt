package org.example.tlbglxt.service;

import org.example.tlbglxt.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface MenuService {

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    Menu getMenuById(Long id);

    /**
     * 查询所有启用的菜单
     *
     * @return 菜单列表
     */
    List<Menu> getAllActiveMenus();

    /**
     * 根据父ID查询子菜单
     *
     * @param parentId 父菜单ID
     * @return 菜单列表
     */
    List<Menu> getMenusByParentId(Long parentId);

    /**
     * 根据用户ID查询有权限的菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> getMenusByUserId(Long userId);

    /**
     * 根据角色ID查询菜单
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<Menu> getMenusByRoleId(Long roleId);

    /**
     * 根据菜单类型查询菜单
     *
     * @param menuType 菜单类型
     * @return 菜单列表
     */
    List<Menu> getMenusByType(Integer menuType);

    /**
     * 构建菜单树
     *
     * @param menus 菜单列表
     * @return 菜单树
     */
    List<Menu> buildMenuTree(List<Menu> menus);

    /**
     * 创建菜单
     *
     * @param menu 菜单信息
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean createMenu(Menu menu, Long operatorId);

    /**
     * 更新菜单
     *
     * @param menu 菜单信息
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean updateMenu(Menu menu, Long operatorId);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean deleteMenu(Long id, Long operatorId);

    /**
     * 检查菜单编码是否存在
     *
     * @param menuCode 菜单编码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean isMenuCodeExists(String menuCode, Long excludeId);

    /**
     * 检查路径是否存在
     *
     * @param path 路径
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean isPathExists(String path, Long excludeId);

    /**
     * 检查是否有子菜单
     *
     * @param parentId 父菜单ID
     * @return 是否有子菜单
     */
    boolean hasChildren(Long parentId);
} 
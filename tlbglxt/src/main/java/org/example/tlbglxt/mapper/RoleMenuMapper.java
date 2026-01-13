package org.example.tlbglxt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.tlbglxt.entity.RoleMenu;

import java.util.List;

/**
 * 角色菜单关联数据访问层
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Mapper
public interface RoleMenuMapper {

    /**
     * 根据角色ID查询角色菜单关联
     *
     * @param roleId 角色ID
     * @return 角色菜单关联列表
     */
    List<RoleMenu> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID查询角色菜单关联
     *
     * @param menuId 菜单ID
     * @return 角色菜单关联列表
     */
    List<RoleMenu> selectByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据角色ID列表查询菜单ID列表
     *
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 检查角色是否拥有指定菜单权限
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 是否存在关联
     */
    boolean existsByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 插入角色菜单关联
     *
     * @param roleMenu 角色菜单关联信息
     * @return 影响行数
     */
    int insert(RoleMenu roleMenu);

    /**
     * 批量插入角色菜单关联
     *
     * @param roleMenus 角色菜单关联列表
     * @return 影响行数
     */
    int batchInsert(@Param("roleMenus") List<RoleMenu> roleMenus);

    /**
     * 删除角色菜单关联
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 影响行数
     */
    int deleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 根据角色ID删除所有菜单关联
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID删除所有角色关联
     *
     * @param menuId 菜单ID
     * @return 影响行数
     */
    int deleteByMenuId(@Param("menuId") Long menuId);
} 
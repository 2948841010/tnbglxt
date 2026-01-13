package org.example.tlbglxt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.tlbglxt.entity.Menu;

import java.util.List;

/**
 * 菜单数据访问层
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Mapper
public interface MenuMapper {

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    Menu selectById(@Param("id") Long id);

    /**
     * 查询所有启用的菜单
     *
     * @return 菜单列表
     */
    List<Menu> selectAllActive();

    /**
     * 根据父ID查询子菜单
     *
     * @param parentId 父菜单ID
     * @return 菜单列表
     */
    List<Menu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据用户ID查询有权限的菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<Menu> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单类型查询菜单
     *
     * @param menuType 菜单类型
     * @return 菜单列表
     */
    List<Menu> selectByMenuType(@Param("menuType") Integer menuType);

    /**
     * 插入菜单
     *
     * @param menu 菜单信息
     * @return 影响行数
     */
    int insert(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单信息
     * @return 影响行数
     */
    int updateById(Menu menu);

    /**
     * 删除菜单（逻辑删除）
     *
     * @param id 菜单ID
     * @param updateBy 更新人
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id, @Param("updateBy") Long updateBy);

    /**
     * 检查菜单编码是否存在
     *
     * @param menuCode 菜单编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByMenuCode(@Param("menuCode") String menuCode, @Param("excludeId") Long excludeId);

    /**
     * 检查路径是否存在
     *
     * @param path 路径
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByPath(@Param("path") String path, @Param("excludeId") Long excludeId);

    /**
     * 检查是否有子菜单
     *
     * @param parentId 父菜单ID
     * @return 是否有子菜单
     */
    boolean hasChildren(@Param("parentId") Long parentId);
} 
package org.example.tlbglxt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.tlbglxt.entity.Role;

import java.util.List;

/**
 * 角色数据访问层
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    Role selectById(@Param("id") Long id);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    List<Role> selectAllActive();

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 插入角色
     *
     * @param role 角色信息
     * @return 影响行数
     */
    int insert(Role role);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @return 影响行数
     */
    int updateById(Role role);

    /**
     * 删除角色（逻辑删除）
     *
     * @param id 角色ID
     * @param updateBy 更新人
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id, @Param("updateBy") Long updateBy);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByRoleCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);
} 
package org.example.tlbglxt.service;

import org.example.tlbglxt.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface RoleService {

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    Role getRoleById(Long id);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Role getRoleByCode(String roleCode);

    /**
     * 查询所有启用的角色
     *
     * @return 角色列表
     */
    List<Role> getAllActiveRoles();

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 创建角色
     *
     * @param role 角色信息
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean createRole(Role role, Long operatorId);

    /**
     * 更新角色
     *
     * @param role 角色信息
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean updateRole(Role role, Long operatorId);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean deleteRole(Long id, Long operatorId);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    boolean isRoleCodeExists(String roleCode, Long excludeId);
} 
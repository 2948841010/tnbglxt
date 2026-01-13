package org.example.tlbglxt.service.impl;

import org.example.tlbglxt.entity.Role;
import org.example.tlbglxt.mapper.RoleMapper;
import org.example.tlbglxt.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Role getRoleById(Long id) {
        if (id == null) {
            return null;
        }
        return roleMapper.selectById(id);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return null;
        }
        return roleMapper.selectByRoleCode(roleCode);
    }

    @Override
    public List<Role> getAllActiveRoles() {
        return roleMapper.selectAllActive();
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return roleMapper.selectByUserId(userId);
    }

    @Override
    public boolean createRole(Role role, Long operatorId) {
        if (role == null || !StringUtils.hasText(role.getRoleName()) || 
            !StringUtils.hasText(role.getRoleCode())) {
            return false;
        }

        // 检查角色编码是否已存在
        if (isRoleCodeExists(role.getRoleCode(), null)) {
            throw new RuntimeException("角色编码已存在：" + role.getRoleCode());
        }

        role.setCreateBy(operatorId);
        role.setUpdateBy(operatorId);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setIsDeleted(0);

        if (role.getStatus() == null) {
            role.setStatus(1); // 默认启用
        }
        if (role.getSortOrder() == null) {
            role.setSortOrder(0); // 默认排序
        }

        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean updateRole(Role role, Long operatorId) {
        if (role == null || role.getId() == null) {
            return false;
        }

        // 检查角色编码是否已存在（排除当前记录）
        if (StringUtils.hasText(role.getRoleCode()) && 
            isRoleCodeExists(role.getRoleCode(), role.getId())) {
            throw new RuntimeException("角色编码已存在：" + role.getRoleCode());
        }

        role.setUpdateBy(operatorId);
        role.setUpdateTime(LocalDateTime.now());

        return roleMapper.updateById(role) > 0;
    }

    @Override
    public boolean deleteRole(Long id, Long operatorId) {
        if (id == null) {
            return false;
        }

        return roleMapper.deleteById(id, operatorId) > 0;
    }

    @Override
    public boolean isRoleCodeExists(String roleCode, Long excludeId) {
        if (!StringUtils.hasText(roleCode)) {
            return false;
        }
        return roleMapper.existsByRoleCode(roleCode, excludeId);
    }
} 
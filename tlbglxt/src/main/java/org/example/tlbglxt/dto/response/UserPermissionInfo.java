package org.example.tlbglxt.dto.response;

import lombok.Data;
import org.example.tlbglxt.entity.Menu;
import org.example.tlbglxt.entity.Role;

import java.util.List;

/**
 * 用户权限信息响应DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class UserPermissionInfo {

    /**
     * 用户菜单权限列表
     */
    private List<Menu> menus;

    /**
     * 用户角色列表
     */
    private List<Role> roles;

    /**
     * 权限标识列表（扁平化的权限字符串）
     */
    private List<String> permissions;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;
} 
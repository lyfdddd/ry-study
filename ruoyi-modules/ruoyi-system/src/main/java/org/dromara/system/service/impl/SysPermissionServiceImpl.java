// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：租户常量定义
import org.dromara.common.core.constant.TenantConstants;
// 公共核心服务接口：权限服务接口
import org.dromara.common.core.service.PermissionService;
// Sa-Token工具类：登录助手，提供获取当前登录用户信息的方法
import org.dromara.common.satoken.utils.LoginHelper;
// 系统服务接口：菜单服务接口
import org.dromara.system.service.ISysMenuService;
// 系统服务接口：权限服务接口
import org.dromara.system.service.ISysPermissionService;
// 系统服务接口：角色服务接口
import org.dromara.system.service.ISysRoleService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合类：HashSet集合
import java.util.HashSet;
// Java集合类：Set集合
import java.util.Set;

/**
 * 用户权限服务实现类
 * 实现用户权限查询的核心业务逻辑，包括角色权限和菜单权限
 * 同时实现PermissionService接口，为其他模块提供权限查询服务
 *
 * @author ruoyi
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysPermissionServiceImpl implements ISysPermissionService, PermissionService {

    // 角色服务接口，用于查询角色权限
    private final ISysRoleService roleService;
    // 菜单服务接口，用于查询菜单权限
    private final ISysMenuService menuService;

    /**
     * 获取角色数据权限
     * 查询指定用户拥有的角色权限标识集合
     *
     * @param userId 用户ID
     * @return 角色权限信息集合
     */
    @Override
    public Set<String> getRolePermission(Long userId) {
        // 创建角色权限集合（HashSet自动去重）
        Set<String> roles = new HashSet<>();
        // 如果是超级管理员，拥有所有权限
        if (LoginHelper.isSuperAdmin(userId)) {
            // 添加超级管理员角色标识
            roles.add(TenantConstants.SUPER_ADMIN_ROLE_KEY);
        } else {
            // 非超级管理员，查询用户拥有的角色权限
            roles.addAll(roleService.selectRolePermissionByUserId(userId));
        }
        // 返回角色权限集合
        return roles;
    }

    /**
     * 获取菜单数据权限
     * 查询指定用户拥有的菜单权限标识集合（如：system:user:add）
     *
     * @param userId 用户ID
     * @return 菜单权限信息集合
     */
    @Override
    public Set<String> getMenuPermission(Long userId) {
        // 创建菜单权限集合（HashSet自动去重）
        Set<String> perms = new HashSet<>();
        // 如果是超级管理员，拥有所有权限
        if (LoginHelper.isSuperAdmin(userId)) {
            // 添加通配符权限标识（表示所有权限）
            perms.add("*:*:*");
        } else {
            // 非超级管理员，查询用户拥有的菜单权限
            perms.addAll(menuService.selectMenuPermsByUserId(userId));
        }
        // 返回菜单权限集合
        return perms;
    }
}

package org.dromara.common.core.constant;

/**
 * 租户常量信息
 * 定义多租户模式下的常量，支持SaaS平台的数据隔离
 *
 * @author Lion Li
 */
public interface TenantConstants {

    /**
     * 超级管理员ID
     * 系统最高权限用户ID，拥有所有租户的管理权限
     * 示例：用户ID为1的用户是超级管理员
     */
    Long SUPER_ADMIN_ID = 1L;

    /**
     * 超级管理员角色 roleKey
     * 超级管理员角色标识，拥有系统级权限
     * 示例：superadmin角色可以管理所有租户
     */
    String SUPER_ADMIN_ROLE_KEY = "superadmin";

    /**
     * 租户管理员角色 roleKey
     * 租户内最高权限角色标识，拥有租户内所有权限
     * 示例：admin角色可以管理当前租户的所有数据
     */
    String TENANT_ADMIN_ROLE_KEY = "admin";

    /**
     * 租户管理员角色名称
     * 租户管理员角色的显示名称，用于前端展示
     * 示例：前端显示为"管理员"
     */
    String TENANT_ADMIN_ROLE_NAME = "管理员";

    /**
     * 默认租户ID
     * 系统初始化时的默认租户标识，通常为000000
     * 示例：新用户注册时默认分配到000000租户
     */
    String DEFAULT_TENANT_ID = "000000";

}

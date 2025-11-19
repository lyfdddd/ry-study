package org.dromara.common.core.constant;

/**
 * 系统常量信息
 * 定义系统级别的状态、类型、标识等常量，统一业务逻辑判断标准
 *
 * @author Lion Li
 */
public interface SystemConstants {

    /**
     * 正常状态
     * 用于表示数据状态正常、启用、有效等场景
     * 示例：用户状态、部门状态、角色状态
     */
    String NORMAL = "0";

    /**
     * 异常/停用状态
     * 用于表示数据状态异常、停用、无效等场景
     * 示例：用户被禁用、部门被停用
     */
    String DISABLE = "1";

    /**
     * 是否为系统默认（是）
     * 用于标识系统内置数据，不可删除
     * 示例：系统内置角色、系统参数
     */
    String YES = "Y";

    /**
     * 是否为系统默认（否）
     * 用于标识用户自定义数据，可删除
     * 示例：用户自定义角色、自定义参数
     */
    String NO = "N";

    /**
     * 是否菜单外链（是）
     * 表示菜单是外部链接，点击后在新窗口打开
     * 示例：跳转到第三方系统
     */
    String YES_FRAME = "0";

    /**
     * 是否菜单外链（否）
     * 表示菜单是内部路由，在系统内跳转
     * 示例：系统内部页面
     */
    String NO_FRAME = "1";

    /**
     * 菜单类型（目录）
     * 用于菜单管理，表示该菜单是目录类型，可包含子菜单
     * 示例：系统管理、权限管理
     */
    String TYPE_DIR = "M";

    /**
     * 菜单类型（菜单）
     * 用于菜单管理，表示该菜单是页面类型，可点击跳转
     * 示例：用户管理、角色管理
     */
    String TYPE_MENU = "C";

    /**
     * 菜单类型（按钮）
     * 用于权限管理，表示该菜单是按钮类型，控制按钮显示/隐藏
     * 示例：新增按钮、删除按钮、导出按钮
     */
    String TYPE_BUTTON = "F";

    /**
     * Layout组件标识
     * 前端路由组件名称，表示使用Layout布局
     * 示例：Layout
     */
    String LAYOUT = "Layout";

    /**
     * ParentView组件标识
     * 前端路由组件名称，表示使用父视图布局
     * 示例：ParentView
     */
    String PARENT_VIEW = "ParentView";

    /**
     * InnerLink组件标识
     * 前端路由组件名称，表示内嵌iframe链接
     * 示例：InnerLink
     */
    String INNER_LINK = "InnerLink";

    /**
     * 超级管理员ID
     * 系统内置的超级管理员用户ID，拥有最高权限
     * 示例：用户ID为1的用户是超级管理员
     */
    Long SUPER_ADMIN_ID = 1L;

    /**
     * 根部门祖级列表
     * 部门树形结构的根节点标识，表示顶级部门
     * 示例：顶级部门的ancestors字段值为"0"
     */
    String ROOT_DEPT_ANCESTORS = "0";

    /**
     * 默认部门ID
     * 新用户注册时默认分配的部门ID
     * 示例：新用户默认分配到ID为100的部门
     */
    Long DEFAULT_DEPT_ID = 100L;

}

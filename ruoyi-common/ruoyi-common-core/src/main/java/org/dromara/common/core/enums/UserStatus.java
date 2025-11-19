package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 * 定义用户账户的状态，用于控制用户登录和访问权限
 *
 * @author ruoyi
 */
// Lombok注解：自动生成getter方法
@Getter
// Lombok注解：生成全参构造函数
@AllArgsConstructor
public enum UserStatus {
    /**
     * 正常状态
     * 用户可以正常登录和操作系统
     * 数据库中存储为"0"
     */
    OK("0", "正常"),

    /**
     * 停用状态
     * 用户被管理员停用，无法登录系统
     * 数据库中存储为"1"
     */
    DISABLE("1", "停用"),

    /**
     * 删除状态
     * 用户被逻辑删除，无法登录系统
     * 数据库中存储为"2"
     */
    DELETED("2", "删除");

    /**
     * 状态码
     * 存储在数据库中的状态值
     */
    private final String code;

    /**
     * 状态描述
     * 前端展示的状态名称
     */
    private final String info;

}

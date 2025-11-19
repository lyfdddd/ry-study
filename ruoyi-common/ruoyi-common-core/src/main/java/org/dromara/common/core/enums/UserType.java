package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.common.core.utils.StringUtils;

/**
 * 用户类型枚举
 * 定义系统中不同类型的用户，用于区分后台管理系统用户和移动客户端用户
 * 主要用于Token生成、权限识别、路由分发等场景
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter方法
@Getter
// Lombok注解：生成全参构造函数
@AllArgsConstructor
public enum UserType {

    /**
     * 后台系统用户
     * 指通过Web后台管理系统登录的用户，如管理员、运营人员
     * 对应的用户类型标识为"sys_user"
     */
    SYS_USER("sys_user"),

    /**
     * 移动客户端用户
     * 指通过移动App或小程序登录的用户，如普通用户、会员
     * 对应的用户类型标识为"app_user"
     */
    APP_USER("app_user");

    /**
     * 用户类型标识
     * 存储在Token中，用于识别用户类型
     * 在权限校验、路由分发等场景使用
     */
    private final String userType;

    /**
     * 根据字符串获取对应的用户类型枚举
     * 使用StringUtils.contains进行模糊匹配，提高容错性
     *
     * @param str 用户类型标识字符串
     * @return 匹配到的UserType枚举
     * @throws RuntimeException 如果未找到匹配的用户类型
     */
    public static UserType getUserType(String str) {
        // 遍历所有枚举值
        for (UserType value : values()) {
            // 使用StringUtils.contains进行包含匹配，而不是精确匹配
            // 这样可以提高容错性，例如"sys_user_admin"也能匹配到SYS_USER
            if (StringUtils.contains(str, value.getUserType())) {
                return value;
            }
        }
        // 如果未找到匹配的用户类型，抛出运行时异常
        throw new RuntimeException("'UserType' not found By " + str);
    }
}

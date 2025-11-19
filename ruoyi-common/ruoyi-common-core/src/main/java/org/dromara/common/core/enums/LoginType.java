package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录类型枚举
 * 定义系统支持的各种登录方式，每种方式有独立的重试限制配置
 * 用于登录失败时的错误提示和重试限制
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter方法
@Getter
// Lombok注解：生成全参构造函数
@AllArgsConstructor
public enum LoginType {

    /**
     * 密码登录
     * 使用用户名+密码登录，支持密码错误重试限制
     * 重试超限提示：user.password.retry.limit.exceed
     * 重试计数提示：user.password.retry.limit.count
     */
    PASSWORD("user.password.retry.limit.exceed", "user.password.retry.limit.count"),

    /**
     * 短信登录
     * 使用手机号+短信验证码登录，支持验证码错误重试限制
     * 重试超限提示：sms.code.retry.limit.exceed
     * 重试计数提示：sms.code.retry.limit.count
     */
    SMS("sms.code.retry.limit.exceed", "sms.code.retry.limit.count"),

    /**
     * 邮箱登录
     * 使用邮箱+验证码登录，支持验证码错误重试限制
     * 重试超限提示：email.code.retry.limit.exceed
     * 重试计数提示：email.code.retry.limit.count
     */
    EMAIL("email.code.retry.limit.exceed", "email.code.retry.limit.count"),

    /**
     * 小程序登录
     * 微信小程序授权登录，无重试限制
     * 重试超限提示：空字符串（不限制）
     * 重试计数提示：空字符串（不限制）
     */
    XCX("", "");

    /**
     * 登录重试超出限制提示
     * 国际化消息Key，用于提示用户登录重试次数已超限
     * 示例：user.password.retry.limit.exceed
     */
    final String retryLimitExceed;

    /**
     * 登录重试限制计数提示
     * 国际化消息Key，用于提示用户剩余重试次数
     * 示例：user.password.retry.limit.count
     */
    final String retryLimitCount;
}

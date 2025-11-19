package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备类型枚举
 * 定义用户登录的设备类型，支持多设备登录和权限控制
 * 与LoginUser中的userType配合使用，实现多用户体系
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter方法
@Getter
// Lombok注解：生成全参构造函数
@AllArgsConstructor
public enum DeviceType {

    /**
     * PC端
     * 电脑浏览器访问，如Chrome、Firefox、Edge等
     * 示例：用户通过电脑登录后台管理系统
     */
    PC("pc"),

    /**
     * APP端
     * 移动应用访问，如Android、iOS原生应用
     * 示例：用户通过手机APP登录
     */
    APP("app"),

    /**
     * 小程序端
     * 微信小程序、支付宝小程序等
     * 示例：用户通过微信小程序登录
     */
    XCX("xcx"),

    /**
     * 第三方社交登录平台
     * OAuth2第三方授权登录，如微信、QQ、GitHub等
     * 示例：用户通过微信扫码登录
     */
    SOCIAL("social");

    /**
     * 设备标识字符串
     * 存储在Token的扩展信息中，用于区分设备类型
     * 示例：pc、app、xcx、social
     */
    private final String device;
}

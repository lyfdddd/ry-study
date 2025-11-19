package org.dromara.common.log.enums;

/**
 * 操作人类别枚举
 * 定义操作日志中操作人的类型分类，用于区分不同来源的操作
 * 用于操作日志记录时标识操作来源
 *
 * @author ruoyi
 */
public enum OperatorType {
    /**
     * 其他类型
     * 表示未知或未分类的操作来源
     */
    OTHER,

    /**
     * 后台管理用户
     * 表示来自管理后台的操作
     */
    MANAGE,

    /**
     * 移动端用户
     * 表示来自手机APP的操作
     */
    MOBILE
}

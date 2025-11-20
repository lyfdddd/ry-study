package org.dromara.common.log.enums;

/**
 * 操作状态枚举
 * 定义业务操作的状态分类，用于操作日志记录
 * 通过枚举值标识操作的成功或失败状态
 *
 * @author ruoyi
 */
public enum BusinessStatus {
    /**
     * 成功状态
     * 表示业务操作执行成功
     */
    SUCCESS,

    /**
     * 失败状态
     * 表示业务操作执行失败
     */
    FAIL,
}

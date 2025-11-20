package org.dromara.common.tenant.exception;

import org.dromara.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 租户异常类
 *
 * @author Lion Li
 */
// 继承BaseException，提供租户相关的异常处理
public class TenantException extends BaseException {

    // 序列化版本号，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    // 构造函数，创建租户异常
    // module: 模块名称为"tenant"
    // code: 错误代码
    // args: 错误参数，用于格式化错误消息
    // cause: 异常原因（此处为null）
    public TenantException(String code, Object... args) {
        super("tenant", code, args, null);
    }
}

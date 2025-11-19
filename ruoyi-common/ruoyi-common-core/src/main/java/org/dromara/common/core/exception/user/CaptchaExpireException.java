package org.dromara.common.core.exception.user;

import java.io.Serial;

/**
 * 验证码失效异常类
 * 当验证码已过期或已被使用时抛出此异常
 * 继承自UserException，指定错误码为"user.jcaptcha.expire"
 *
 * @author ruoyi
 */
public class CaptchaExpireException extends UserException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * 调用父类UserException的构造方法，指定错误码
     * 错误码"user.jcaptcha.expire"对应国际化消息："验证码已失效"
     */
    public CaptchaExpireException() {
        // 调用父类构造方法，错误码为"user.jcaptcha.expire"
        // 国际化消息示例：验证码已失效，请重新获取
        super("user.jcaptcha.expire");
    }
}

package org.dromara.common.core.exception.user;

import java.io.Serial;

/**
 * 验证码错误异常类
 * 当用户输入的验证码与系统生成的验证码不匹配时抛出此异常
 * 继承自UserException，指定错误码为"user.jcaptcha.error"
 *
 * @author ruoyi
 */
public class CaptchaException extends UserException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * 调用父类UserException的构造方法，指定错误码
     * 错误码"user.jcaptcha.error"对应国际化消息："验证码错误"
     */
    public CaptchaException() {
        // 调用父类构造方法，错误码为"user.jcaptcha.error"
        // 国际化消息示例：验证码错误，请重新输入
        super("user.jcaptcha.error");
    }
}

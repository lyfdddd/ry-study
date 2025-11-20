package org.dromara.common.web.enums;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 验证码类别
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter方法
@Getter
// Lombok注解，自动生成全参构造函数
@AllArgsConstructor
// 验证码类别枚举，定义不同类型的验证码干扰方式
public enum CaptchaCategory {

    /**
     * 线段干扰
     */
    // 线段干扰验证码，使用LineCaptcha类生成
    LINE(LineCaptcha.class),

    /**
     * 圆圈干扰
     */
    // 圆圈干扰验证码，使用CircleCaptcha类生成
    CIRCLE(CircleCaptcha.class),

    /**
     * 扭曲干扰
     */
    // 扭曲干扰验证码，使用ShearCaptcha类生成
    SHEAR(ShearCaptcha.class);

    // 验证码实现类，继承自AbstractCaptcha
    private final Class<? extends AbstractCaptcha> clazz;
}

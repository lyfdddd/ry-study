package org.dromara.common.web.config.properties;

import org.dromara.common.web.enums.CaptchaCategory;
import org.dromara.common.web.enums.CaptchaType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码 配置属性
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString等方法
@Data
// Spring Boot配置属性注解，将配置文件中captcha前缀的属性绑定到此类
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    // 验证码功能开关，true表示启用，false表示禁用
    private Boolean enable;

    /**
     * 验证码类型
     */
    // 验证码类型，如数字验证码、字符验证码、数学表达式验证码等
    private CaptchaType type;

    /**
     * 验证码类别
     */
    // 验证码类别，如线段干扰、圆圈干扰、扭曲干扰等
    private CaptchaCategory category;

    /**
     * 数字验证码位数
     */
    // 数字验证码的位数，如4位、6位等
    private Integer numberLength;

    /**
     * 字符验证码长度
     */
    // 字符验证码的长度，如4位、6位等
    private Integer charLength;
}

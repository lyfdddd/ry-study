package org.dromara.common.web.config;

// 导入Hutool验证码工具类，提供验证码创建便捷方法
// CaptchaUtil是Hutool封装的验证码工具类，提供静态方法快速创建各种类型的验证码
import cn.hutool.captcha.CaptchaUtil;
// 导入圆圈干扰验证码类，生成带有圆圈干扰的验证码图片
// CircleCaptcha继承自AbstractCaptcha，使用圆圈作为干扰元素
import cn.hutool.captcha.CircleCaptcha;
// 导入线段干扰验证码类，生成带有线段干扰的验证码图片
// LineCaptcha继承自AbstractCaptcha，使用随机线段作为干扰元素
import cn.hutool.captcha.LineCaptcha;
// 导入扭曲干扰验证码类，生成带有扭曲效果的验证码图片
// ShearCaptcha继承自AbstractCaptcha，使用剪切扭曲算法生成干扰效果
import cn.hutool.captcha.ShearCaptcha;
// 导入验证码配置属性类，用于读取配置文件中的验证码相关配置
// CaptchaProperties从application.yml中读取captcha前缀的配置项
import org.dromara.common.web.config.properties.CaptchaProperties;
// 导入Spring Boot自动配置注解，标识此类为自动配置类
// @AutoConfiguration是Spring Boot 2.7+引入的注解，替代@Configuration
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入启用配置属性注解，使CaptchaProperties配置类生效
// @EnableConfigurationProperties用于启用@ConfigurationProperties注解的类
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// 导入Bean注解，用于声明Spring Bean
// @Bean注解将方法返回的对象注册为Spring容器管理的Bean
import org.springframework.context.annotation.Bean;
// 导入懒加载注解，实现Bean的延迟初始化
// @Lazy注解实现Bean的懒加载，首次使用时才创建实例，提升启动速度
import org.springframework.context.annotation.Lazy;

// 导入Java AWT包，用于设置字体和颜色
// Color类表示颜色，Font类表示字体
import java.awt.*; // 导入Color和Font类

/**
 * 验证码配置类
 * 配置系统中使用的各种类型验证码（圆圈干扰、线段干扰、扭曲干扰）
 * 使用Hutool工具类创建验证码对象，并统一设置样式
 * 提供三种验证码类型的Bean，供业务层根据配置动态选择使用
 *
 * @author Lion Li
 */
// @AutoConfiguration注解：标识此类为Spring Boot自动配置类，Spring Boot启动时会自动加载
// 自动配置类会在满足条件时自动装配Bean，无需手动配置
@AutoConfiguration
// @EnableConfigurationProperties注解：启用CaptchaProperties配置属性类
// 将配置文件中的captcha前缀属性绑定到CaptchaProperties类，实现类型安全的配置
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaConfig {

    /**
     * 验证码图片宽度常量
     * 单位：像素，设置为160像素
     * 这个宽度适合在网页上显示，既不会太小导致难以识别，也不会太大占用过多空间
     */
    private static final int WIDTH = 160;
    
    /**
     * 验证码图片高度常量
     * 单位：像素，设置为60像素
     * 这个高度与宽度比例协调，确保验证码字符清晰可辨
     */
    private static final int HEIGHT = 60;
    
    /**
     * 验证码背景颜色常量
     * 设置为浅灰色（Color.LIGHT_GRAY）
     * 浅灰色背景与黑色字符形成良好对比，提升可读性
     */
    private static final Color BACKGROUND = Color.LIGHT_GRAY;
    
    /**
     * 验证码字体常量
     * 使用Arial字体，粗体样式，48号字
     * Arial是通用字体，粗体确保字符清晰，48号字大小适中
     */
    private static final Font FONT = new Font("Arial", Font.BOLD, 48);

    /**
     * 创建圆圈干扰验证码Bean
     * 使用Hutool的CaptchaUtil创建带有圆圈干扰的验证码
     * 懒加载（@Lazy），首次使用时才创建实例
     * 圆圈干扰验证码使用随机圆圈作为干扰元素，增加机器识别难度
     *
     * @return CircleCaptcha 圆圈干扰验证码对象
     */
    // @Lazy注解：实现Bean的懒加载，首次使用时才创建实例，提升启动速度
    // 懒加载避免在应用启动时创建不必要的对象，减少内存占用和启动时间
    @Lazy
    // @Bean注解：声明此方法返回的对象为Spring Bean，交给Spring容器管理
    // Spring容器会管理Bean的生命周期，包括创建、初始化、销毁
    @Bean
    public CircleCaptcha circleCaptcha() {
        // 使用Hutool工具类的CaptchaUtil.createCircleCaptcha方法创建圆圈干扰验证码
        // 参数：宽度、高度（单位：像素），使用预定义的WIDTH和HEIGHT常量
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(WIDTH, HEIGHT);
        // 设置验证码背景颜色，使用预定义的浅灰色BACKGROUND常量
        // 背景颜色影响验证码的可读性和美观度
        captcha.setBackground(BACKGROUND);
        // 设置验证码字体，使用预定义的Arial粗体48号字FONT常量
        // 字体设置确保验证码字符清晰可辨
        captcha.setFont(FONT);
        // 返回配置完成的验证码对象
        // 此对象会被Spring容器管理，可以在其他组件中注入使用
        return captcha;
    }

    /**
     * 创建线段干扰验证码Bean
     * 使用Hutool的CaptchaUtil创建带有线段干扰的验证码
     * 懒加载，首次使用时才创建实例
     * 线段干扰验证码使用随机线段作为干扰元素，增加机器识别难度
     *
     * @return LineCaptcha 线段干扰验证码对象
     */
    // @Lazy注解：实现Bean的懒加载，避免不必要的对象创建
    @Lazy
    // @Bean注解：声明Spring Bean，交给Spring容器管理
    @Bean
    public LineCaptcha lineCaptcha() {
        // 使用Hutool工具类的CaptchaUtil.createLineCaptcha方法创建线段干扰验证码
        // 参数：宽度、高度（单位：像素），使用预定义的WIDTH和HEIGHT常量
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT);
        // 设置验证码背景颜色为浅灰色，使用预定义的BACKGROUND常量
        captcha.setBackground(BACKGROUND);
        // 设置验证码字体为Arial粗体48号字，使用预定义的FONT常量
        captcha.setFont(FONT);
        // 返回配置完成的验证码对象
        return captcha;
    }

    /**
     * 创建扭曲干扰验证码Bean
     * 使用Hutool的CaptchaUtil创建带有扭曲效果的验证码
     * 懒加载，首次使用时才创建实例
     * 扭曲干扰验证码使用剪切扭曲算法生成干扰效果，增加机器识别难度
     *
     * @return ShearCaptcha 扭曲干扰验证码对象
     */
    // @Lazy注解：实现Bean的懒加载，提升应用启动性能
    @Lazy
    // @Bean注解：声明Spring Bean，实现依赖注入
    @Bean
    public ShearCaptcha shearCaptcha() {
        // 使用Hutool工具类的CaptchaUtil.createShearCaptcha方法创建扭曲干扰验证码
        // 参数：宽度、高度（单位：像素），使用预定义的WIDTH和HEIGHT常量
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(WIDTH, HEIGHT);
        // 设置验证码背景颜色为浅灰色，使用预定义的BACKGROUND常量
        captcha.setBackground(BACKGROUND);
        // 设置验证码字体为Arial粗体48号字，使用预定义的FONT常量
        captcha.setFont(FONT);
        // 返回配置完成的验证码对象
        return captcha;
    }

}

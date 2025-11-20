package org.dromara.common.encrypt.annotation;

// 导入Java语言注解相关类
import java.lang.annotation.*;

/**
 * API强制加密注解
 * 用于标记需要加密的方法，控制接口响应数据的加密行为
 *
 * @author Michelle.Chung
 */
// 表示该注解会被javadoc工具记录
@Documented
// 指定注解作用目标为方法
@Target({ElementType.METHOD})
// 指定注解保留策略为运行时，可通过反射获取
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {

    /**
     * 是否对响应数据进行加密
     * 默认为false不加密，设置为true时启用加密
     */
    boolean response() default false;

}

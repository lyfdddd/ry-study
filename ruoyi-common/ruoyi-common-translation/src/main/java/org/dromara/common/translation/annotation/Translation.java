package org.dromara.common.translation.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.dromara.common.translation.core.handler.TranslationHandler;

import java.lang.annotation.*;

/**
 * 通用翻译注解
 *
 * @author Lion Li
 */
// 表示注解可以被继承
@Inherited
// 注解在运行时保留，可以通过反射获取
@Retention(RetentionPolicy.RUNTIME)
// 注解可以应用于字段和方法
@Target({ElementType.FIELD, ElementType.METHOD})
// 注解会被javadoc工具记录
@Documented
// Jackson内部注解，表示这是一个组合注解
@JacksonAnnotationsInside
// 指定使用TranslationHandler进行序列化
@JsonSerialize(using = TranslationHandler.class)
public @interface Translation {

    /**
     * 类型 (需与实现类上的 {@link TranslationType} 注解type对应)
     * <p>
     * 默认取当前字段的值 如果设置了 @{@link Translation#mapper()} 则取映射字段的值
     */
    // 翻译类型，对应TranslationType注解的type值，用于匹配具体的翻译实现类
    String type();

    /**
     * 映射字段 (如果不为空则取此字段的值)
     */
    // 指定从哪个字段获取需要翻译的原始值，为空则使用当前字段的值
    String mapper() default "";

    /**
     * 其他条件 例如: 字典type(sys_user_sex)
     */
    // 其他附加条件，如字典类型、部门ID等，用于特定翻译场景
    String other() default "";

}

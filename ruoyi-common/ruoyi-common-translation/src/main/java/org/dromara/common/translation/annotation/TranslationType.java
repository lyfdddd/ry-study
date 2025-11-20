package org.dromara.common.translation.annotation;

import org.dromara.common.translation.core.TranslationInterface;

import java.lang.annotation.*;

/**
 * 翻译类型注解 (标注到{@link TranslationInterface} 的实现类)
 *
 * @author Lion Li
 */
// 表示注解可以被继承
@Inherited
// 注解在运行时保留，可以通过反射获取
@Retention(RetentionPolicy.RUNTIME)
// 注解只能应用于类、接口、枚举类型
@Target({ElementType.TYPE})
// 注解会被javadoc工具记录
@Documented
public @interface TranslationType {

    /**
     * 类型
     */
    // 翻译类型标识，用于匹配Translation注解的type值
    String type();

}

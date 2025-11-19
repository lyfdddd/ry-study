// 枚举值校验注解定义，基于Jakarta Bean Validation规范
package org.dromara.common.core.validate.enumd;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义枚举校验注解
 * 基于Jakarta Bean Validation规范，用于校验字段值是否在指定的枚举类型范围内
 * 通过EnumPatternValidator实现具体的校验逻辑
 * 可以标注在方法、字段、构造函数、参数等级别
 * 用于确保用户输入的值是枚举中定义的有效值，防止非法数据
 * 支持通过指定枚举字段名进行校验，不仅限于name()方法
 * 支持在同一个元素上多次使用该注解（通过@Repeatable实现）
 *
 * @author 秋辞未寒
 * @date 2024-12-09
 */
// 文档化注解，包含在Javadoc中
@Documented
// 可以标注的目标元素类型：方法、字段、注解类型、构造函数、参数、类型使用
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
// 运行时保留的注解，可以在运行期通过反射获取
@Retention(RetentionPolicy.RUNTIME)
// 允许在同一元素上多次使用该注解，通过List容器注解实现
@Repeatable(EnumPattern.List.class)
// 指定校验器实现类为EnumPatternValidator
@Constraint(validatedBy = {EnumPatternValidator.class})
public @interface EnumPattern {

    /**
     * 需要校验的枚举类型
     * 指定要校验的枚举类，必须是Enum的子类
     * 这是必填属性，必须指定具体的枚举类型
     */
    // 枚举类型，必须是Enum的子类
    Class<? extends Enum<?>> type();

    /**
     * 枚举类型校验值字段名称
     * 需确保该字段实现了getter方法
     * 例如：code、value、label等自定义字段
     * 不限于枚举的name()方法，可以指定任意有getter方法的字段
     */
    // 枚举字段名，需要有对应的getter方法
    String fieldName();

    /**
     * 默认校验失败提示信息
     * 当校验不通过时返回的错误信息
     * 可以在使用时通过message属性自定义错误信息
     */
    // 校验失败时的错误提示信息
    String message() default "输入值不在枚举范围内";

    /**
     * 校验分组
     * 用于分组校验，可以根据不同的业务场景使用不同的校验规则
     * 例如：新增时使用AddGroup，修改时使用EditGroup
     */
    // 校验分组，默认为空数组
    Class<?>[] groups() default {};

    /**
     * 负载
     * 用于携带额外的元数据信息，通常用于客户端传递额外信息
     * 符合Jakarta Bean Validation规范的要求
     */
    // 负载，用于携带额外信息
    Class<? extends Payload>[] payload() default {};

    /**
     * 容器注解，用于支持@Repeatable
     * 当同一个元素上多次使用@EnumPattern时，会被包装成@List
     * 这是Java 8引入的可重复注解机制
     */
    // 容器注解，支持可重复注解
    @Documented
    // 目标元素类型与@EnumPattern保持一致
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
    // 运行时保留
    @Retention(RetentionPolicy.RUNTIME)
    // 容器注解定义
    @interface List {
        /**
         * 包含的@EnumPattern注解数组
         * 当同一个元素上多次使用@EnumPattern时，所有实例会存储在这个数组中
         */
        // @EnumPattern注解数组
        EnumPattern[] value();
    }

}

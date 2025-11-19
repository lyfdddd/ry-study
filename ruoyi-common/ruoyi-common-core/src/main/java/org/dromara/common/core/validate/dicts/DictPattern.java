// 字典项校验注解定义，基于Jakarta Bean Validation规范
package org.dromara.common.core.validate.dicts;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典项校验注解
 * 基于Jakarta Bean Validation规范，用于校验字段值是否在指定的字典类型范围内
 * 通过DictPatternValidator实现具体的校验逻辑
 * 可以标注在字段、方法参数等级别
 * 用于确保用户输入的值是系统中预定义的字典值，防止非法数据
 * 支持单值和多值（逗号分隔）的字典校验
 *
 * @author AprilWind
 */
// 指定校验器实现类为DictPatternValidator
@Constraint(validatedBy = DictPatternValidator.class)
// 可以标注的目标元素类型：字段、方法参数
@Target({ElementType.FIELD, ElementType.PARAMETER})
// 运行时保留的注解，可以在运行期通过反射获取
@Retention(RetentionPolicy.RUNTIME)
public @interface DictPattern {

    /**
     * 字典类型，如 "sys_user_sex"、"sys_normal_disable"等
     * 对应系统中定义的字典类型，用于指定要校验的字典范围
     * 这是必填属性，必须指定具体的字典类型
     */
    // 字典类型，如"sys_user_sex"
    String dictType();

    /**
     * 分隔符
     * 用于多值字典的分隔，默认为逗号","
     * 例如：多选框的值可能是"1,2,3"，需要用分隔符拆分后逐个校验
     * 如果不需要多值校验，可以设置为空字符串
     */
    // 分隔符，用于多值字典
    String separator();

    /**
     * 默认校验失败提示信息
     * 当校验不通过时返回的错误信息
     * 可以在使用时通过message属性自定义错误信息
     */
    // 校验失败时的错误提示信息
    String message() default "字典值无效";

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

}

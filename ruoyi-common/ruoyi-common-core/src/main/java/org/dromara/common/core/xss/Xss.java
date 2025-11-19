// XSS防护校验注解定义，基于Jakarta Bean Validation规范
package org.dromara.common.core.xss;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义XSS校验注解
 * 基于Jakarta Bean Validation规范，用于防止跨站脚本攻击（XSS）
 * 可以标注在字段、方法、构造函数、参数等级别
 * 通过XssValidator实现具体的校验逻辑
 * 当检测到输入值包含HTML标签时，校验失败并返回错误信息
 *
 * @author Lion Li
 */
// 运行时保留的注解，可以在运行期通过反射获取
@Retention(RetentionPolicy.RUNTIME)
// 可以标注的目标元素类型：方法、字段、构造函数、参数
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
// 指定校验器实现类为XssValidator
@Constraint(validatedBy = {XssValidator.class})
public @interface Xss {

    /**
     * 校验失败时的默认错误信息
     * 可以在使用时通过message属性自定义错误信息
     * 默认值为"不允许任何脚本运行"
     */
    // 校验失败时的错误提示信息
    String message() default "不允许任何脚本运行";

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

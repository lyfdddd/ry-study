package org.dromara.common.json.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * JSON 格式校验注解
 * 用于校验字段值是否为合法的JSON格式，支持对象、数组或任意类型
 *
 * @author AprilWind
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonPatternValidator.class)
public @interface JsonPattern {

    /**
     * 限制 JSON 类型，默认为 {@link JsonType#ANY}，即对象或数组都允许
     * 通过枚举指定允许的JSON类型
     */
    JsonType type() default JsonType.ANY;

    /**
     * 校验失败时的提示消息
     * 默认提示"不是有效的 JSON 格式"
     */
    String message() default "不是有效的 JSON 格式";

    /**
     * 校验分组，用于分组校验
     */
    Class<?>[] groups() default {};

    /**
     * 负载，用于传递额外信息
     */
    Class<? extends Payload>[] payload() default {};

}

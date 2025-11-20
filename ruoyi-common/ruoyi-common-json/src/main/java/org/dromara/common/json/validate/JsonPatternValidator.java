package org.dromara.common.json.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;

/**
 * JSON 格式校验器
 * 实现Jakarta Bean Validation规范，用于校验JSON格式
 *
 * @author AprilWind
 */
public class JsonPatternValidator implements ConstraintValidator<JsonPattern, String> {

    /**
     * 注解中指定的 JSON 类型枚举
     * 存储从@JsonPattern注解中提取的JSON类型
     */
    private JsonType jsonType;

    /**
     * 初始化校验器，从注解中提取 JSON 类型
     * 在校验器初始化时调用，获取注解配置
     *
     * @param annotation 注解实例
     */
    @Override
    public void initialize(JsonPattern annotation) {
        // 从注解中获取JSON类型配置
        this.jsonType = annotation.type();
    }

    /**
     * 校验字符串是否为合法 JSON
     * 实现具体的校验逻辑，根据配置的JSON类型进行校验
     *
     * @param value   待校验字符串
     * @param context 校验上下文，可用于自定义错误信息
     * @return true = 合法 JSON 或为空，false = 非法 JSON
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 判断字符串是否为空，如果为空则返回true
        // 空值校验交给 @NotBlank 或 @NotNull 注解处理
        if (StringUtils.isBlank(value)) {
            return true;
        }
        // 根据 JSON 类型进行不同的校验，使用switch表达式
        return switch (jsonType) {
            // 任意JSON类型（对象或数组）
            case ANY -> JsonUtils.isJson(value);
            // 仅JSON对象
            case OBJECT -> JsonUtils.isJsonObject(value);
            // 仅JSON数组
            case ARRAY -> JsonUtils.isJsonArray(value);
        };
    }

}

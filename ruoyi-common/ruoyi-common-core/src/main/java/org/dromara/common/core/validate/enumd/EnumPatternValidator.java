// 枚举值校验器实现类，基于反射机制进行枚举值合法性验证
package org.dromara.common.core.validate.enumd;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.reflect.ReflectUtils;

/**
 * 自定义枚举校验注解实现
 * 基于反射机制进行枚举值合法性验证，确保输入值在指定的枚举类型范围内
 * 实现Jakarta Bean Validation的ConstraintValidator接口
 * 可以标注在字段、方法参数等级别
 * 用于确保用户输入的值是枚举中定义的有效值，防止非法数据
 * 支持通过指定枚举字段名进行校验，不仅限于name()方法
 *
 * @author 秋辞未寒
 * @date 2024-12-09
 */
// 枚举值校验器实现类，实现ConstraintValidator接口
public class EnumPatternValidator implements ConstraintValidator<EnumPattern, String> {

    /**
     * 枚举校验注解实例
     * 存储@EnumPattern注解的配置信息，包括枚举类型和字段名
     */
    private EnumPattern annotation;

    /**
     * 初始化校验器，提取注解上的枚举类型和字段名
     * 该方法在校验器实例化时调用，用于获取注解配置信息
     *
     * @param annotation 注解实例，包含type和fieldName配置
     */
    @Override
    public void initialize(EnumPattern annotation) {
        // 调用父类的初始化方法
        ConstraintValidator.super.initialize(annotation);
        // 保存注解实例，供后续校验使用
        this.annotation = annotation;
    }

    /**
     * 校验字段值是否为指定枚举类型中的合法值
     * 通过反射调用枚举的getter方法，检查输入值是否匹配枚举中的某个字段值
     * 支持自定义字段名，不仅限于枚举的name()方法
     *
     * @param value 被校验的字段值
     * @param constraintValidatorContext 校验上下文，可用于构建自定义错误信息
     * @return true 表示校验通过（合法枚举值），false 表示不通过
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 如果输入值不为空，进行枚举值校验
        // StringUtils.isNotBlank会检查字符串是否为null、空字符串和"null"字符串
        if (StringUtils.isNotBlank(value)) {
            // 从注解中获取要校验的枚举字段名
            // 例如：code、value、label等自定义字段
            String fieldName = annotation.fieldName();
            // 获取枚举类型的所有常量实例
            // annotation.type()返回枚举类，getEnumConstants()返回所有枚举实例
            for (Object e : annotation.type().getEnumConstants()) {
                // 使用反射工具类调用枚举实例的getter方法获取字段值
                // 例如：调用getCode()、getValue()等方法
                // 将返回值与输入值进行比较
                if (value.equals(ReflectUtils.invokeGetter(e, fieldName))) {
                    // 如果找到匹配的值，说明输入值是合法的枚举值，返回true
                    return true;
                }
            }
        }
        // 如果输入值为空，或者遍历完所有枚举实例都没有找到匹配的值，返回false
        // 表示校验不通过
        return false;
    }

}

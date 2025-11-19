// 字典值校验器实现类，基于字典服务进行值合法性验证
package org.dromara.common.core.validate.dicts;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.dromara.common.core.service.DictService;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;

/**
 * 自定义字典值校验器
 * 基于字典服务进行值合法性验证，确保输入值在指定的字典类型范围内
 * 实现Jakarta Bean Validation的ConstraintValidator接口
 * 支持单值和多值（逗号分隔）的字典校验
 * 用于确保用户输入的值是系统中预定义的字典值，防止非法数据
 *
 * @author AprilWind
 */
// 字典值校验器实现类，实现ConstraintValidator接口
public class DictPatternValidator implements ConstraintValidator<DictPattern, String> {

    /**
     * 字典类型
     * 从@DictPattern注解中获取，如"sys_user_sex"、"sys_normal_disable"等
     * 用于指定要校验的字典类型
     */
    private String dictType;

    /**
     * 分隔符
     * 用于多值字典的分隔，默认为逗号","
     * 例如：多选框的值可能是"1,2,3"，需要用分隔符拆分后逐个校验
     */
    private String separator = ",";

    /**
     * 初始化校验器，提取注解上的字典类型和分隔符
     * 该方法在校验器实例化时调用，用于获取注解配置信息
     *
     * @param annotation 注解实例，包含dictType和separator配置
     */
    @Override
    public void initialize(DictPattern annotation) {
        // 从注解中获取字典类型
        this.dictType = annotation.dictType();
        // 如果注解中指定了分隔符且不为空，则使用指定的分隔符
        // 否则使用默认的逗号分隔符
        if (StringUtils.isNotBlank(annotation.separator())) {
            this.separator = annotation.separator();
        }
    }

    /**
     * 校验字段值是否为指定字典类型中的合法值
     * 通过DictService查询字典标签，如果查询结果不为空则认为值合法
     * 支持单值和多值（逗号分隔）的校验
     *
     * @param value   被校验的字段值
     * @param context 校验上下文（可用于构建错误信息）
     * @return true 表示校验通过（合法字典值），false 表示不通过
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果字典类型为空或值为空，直接返回false，校验不通过
        // 字典类型和值都是必填的
        if (StringUtils.isBlank(dictType) || StringUtils.isBlank(value)) {
            return false;
        }
        // 通过SpringUtils获取DictService的Bean实例
        // 调用getDictLabel方法查询字典标签，如果值合法则返回对应的标签
        // 如果值不合法，返回空字符串
        String dictLabel = SpringUtils.getBean(DictService.class).getDictLabel(dictType, value, separator);
        // 如果查询到的字典标签不为空，说明值合法，返回true
        // 否则返回false，校验不通过
        return StringUtils.isNotBlank(dictLabel);
    }

}

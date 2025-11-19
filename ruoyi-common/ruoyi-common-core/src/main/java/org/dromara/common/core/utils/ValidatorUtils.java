// 基于Jakarta Bean Validation的参数校验工具类
package org.dromara.common.core.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Bean Validation校验框架工具类
 * 基于Jakarta Bean Validation（JSR 380）规范，提供统一的参数校验入口
 * 支持分组校验、自定义校验注解等功能
 * 集成Spring的Validator，支持Spring的校验机制
 * 提供统一的异常处理，便于全局异常处理器统一处理
 *
 * @author Lion Li
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorUtils {

    /**
     * Jakarta Bean Validation校验器
     * 从Spring容器中获取Validator单例，用于执行参数校验
     * 支持@NotNull、@NotEmpty、@NotBlank、@Size、@Pattern等标准校验注解
     * 也支持自定义校验注解，如@Email、@Phone等
     * Validator是线程安全的，可以在多线程环境下共享使用
     */
    private static final Validator VALID = SpringUtils.getBean(Validator.class);

    /**
     * 对给定对象进行参数校验，并根据指定的校验组进行校验
     * 如果校验不通过，则抛出ConstraintViolationException异常
     * 支持分组校验，可以根据不同的业务场景使用不同的校验规则
     * 如：新增时使用CreateGroup，修改时使用UpdateGroup
     *
     * @param <T> 被校验对象的泛型类型
     * @param object 要进行校验的对象（通常是DTO、VO或实体类）
     * @param groups 校验组（可选），用于分组校验，如新增组、修改组等
     * @throws ConstraintViolationException 如果校验不通过，则抛出参数校验异常，包含所有校验错误信息
     */
    public static <T> void validate(T object, Class<?>... groups) {
        // 执行参数校验，返回校验结果集合
        // validate方法会检查对象上所有标注了校验注解的字段
        Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
        // 如果校验结果不为空，说明存在校验错误
        if (!validate.isEmpty()) {
            // 抛出ConstraintViolationException异常，包含所有校验错误信息
            // 异常信息会被全局异常处理器捕获并转换为友好的错误提示
            // ConstraintViolationException是Jakarta Validation的标准异常
            throw new ConstraintViolationException("参数校验异常", validate);
        }
    }

}

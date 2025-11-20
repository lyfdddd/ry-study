package org.dromara.common.idempotent.annotation;

// 导入Java语言注解相关类
import java.lang.annotation.*;
// 导入时间单位枚举
import java.util.concurrent.TimeUnit;

/**
 * 防止表单重复提交注解
 * 用于标记需要防止重复提交的方法，通过Redis缓存实现幂等性控制
 *
 * @author Lion Li
 */
// 表示该注解可以被子类继承
@Inherited
// 指定注解作用目标为方法
@Target(ElementType.METHOD)
// 指定注解保留策略为运行时，可通过反射获取
@Retention(RetentionPolicy.RUNTIME)
// 表示该注解会被javadoc工具记录
@Documented
public @interface RepeatSubmit {

    /**
     * 间隔时间，小于此时间视为重复提交
     * 默认值为5000毫秒
     */
    int interval() default 5000;

    /**
     * 时间单位
     * 默认为毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 提示消息
     * 支持国际化，格式为 {code}
     * 默认值为{repeat.submit.message}
     */
    String message() default "{repeat.submit.message}";

}

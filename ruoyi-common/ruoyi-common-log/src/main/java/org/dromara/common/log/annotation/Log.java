// 操作日志记录注解，用于标记需要记录操作日志的方法
// 通过AOP切面拦截注解标记的方法，自动记录操作日志到数据库
package org.dromara.common.log.annotation;

// 业务操作类型枚举，如查询、新增、修改、删除等
import org.dromara.common.log.enums.BusinessType;
// 操作人类别枚举，如后台用户、手机端用户等
import org.dromara.common.log.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 *
 * @author ruoyi
 */
// 注解作用目标：参数和方法
@Target({ElementType.PARAMETER, ElementType.METHOD})
// 注解保留策略：运行时保留，可通过反射读取
@Retention(RetentionPolicy.RUNTIME)
// 注解文档化，生成javadoc时会包含此注解信息
@Documented
public @interface Log {
    /**
     * 模块标题，如"系统管理"、"用户管理"等
     * 用于日志分类和前端展示
     */
    String title() default "";

    /**
     * 业务操作类型，如查询、新增、修改、删除等
     * 使用BusinessType枚举限定取值范围
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别，如后台用户、手机端用户等
     * 使用OperatorType枚举限定取值范围
     */
    OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求参数，默认true
     * 用于记录接口调用时传入的参数，方便问题排查
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应参数，默认true
     * 用于记录接口返回的数据，方便问题排查
     */
    boolean isSaveResponseData() default true;


    /**
     * 排除指定的请求参数，支持多个参数名
     * 用于过滤敏感信息，如密码、支付信息等
     */
    String[] excludeParamNames() default {};

}

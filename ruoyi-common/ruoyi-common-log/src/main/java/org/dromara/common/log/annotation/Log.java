package org.dromara.common.log.annotation;

import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.log.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 * 用于标记需要记录操作日志的方法，通过AOP切面拦截注解标记的方法
 * 自动记录操作日志到数据库，包括请求参数、响应结果、执行时间等信息
 *
 * @author ruoyi
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块标题，如"系统管理"、"用户管理"等
     * 用于日志分类和前端展示，帮助快速定位操作所属模块
     */
    String title() default "";

    /**
     * 业务操作类型，如查询、新增、修改、删除等
     * 使用BusinessType枚举限定取值范围，便于日志统计和分析
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别，如后台用户、手机端用户等
     * 使用OperatorType枚举限定取值范围，区分不同来源的操作
     */
    OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求参数，默认true
     * 用于记录接口调用时传入的参数，方便问题排查和审计
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存响应参数，默认true
     * 用于记录接口返回的数据，方便问题排查和结果验证
     */
    boolean isSaveResponseData() default true;

    /**
     * 排除指定的请求参数，支持多个参数名
     * 用于过滤敏感信息，如密码、支付信息等，防止敏感数据泄露
     */
    String[] excludeParamNames() default {};

}

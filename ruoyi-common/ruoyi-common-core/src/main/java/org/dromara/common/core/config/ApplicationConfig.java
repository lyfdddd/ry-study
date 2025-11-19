// 定义Spring应用的核心注解配置类，集中管理框架级功能开关
package org.dromara.common.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 程序注解配置
 * 集中配置Spring核心注解，启用AOP代理和异步执行能力
 * 该类是Spring Boot自动配置的核心，确保AOP和异步功能正确启用
 * 通过@AutoConfiguration注解，Spring Boot会在启动时自动加载此配置
 *
 * @author Lion Li
 */
// Spring Boot自动配置类，会在应用启动时自动加载
// 无需手动导入，Spring Boot的自动配置机制会扫描并加载
@AutoConfiguration
// 启用AspectJ自动代理，支持AOP切面编程（如@Aspect注解）
// exposeProxy=true表示暴露代理对象，解决同类方法调用AOP失效问题
@EnableAspectJAutoProxy(exposeProxy = true)
// 启用异步方法执行，proxyTargetClass=true表示使用CGLIB代理（基于类）
// 使用CGLIB代理可以代理没有实现接口的类，更加灵活
@EnableAsync(proxyTargetClass = true)
public class ApplicationConfig {

    /**
     * 该类不需要具体的实现方法
     * 通过注解配置的方式为Spring容器提供AOP和异步执行能力
     * 是RuoYi框架中Spring核心功能的基础配置
     */

}

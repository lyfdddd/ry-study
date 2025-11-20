package org.dromara.common.mybatis.aspect;

// Lombok日志注解，自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// AOP联盟方法拦截器接口，用于实现方法拦截逻辑
import org.aopalliance.intercept.MethodInterceptor;
// AOP联盟方法调用接口，封装了被拦截的方法信息
import org.aopalliance.intercept.MethodInvocation;
// 数据权限注解，用于标记需要数据权限控制的方法
import org.dromara.common.mybatis.annotation.DataPermission;
// 数据权限辅助类，用于在ThreadLocal中存取数据权限配置
import org.dromara.common.mybatis.helper.DataPermissionHelper;

// Java反射Method类
import java.lang.reflect.Method;
// Java反射Proxy类，用于判断是否为动态代理类
import java.lang.reflect.Proxy;

/**
 * 数据权限AOP通知类
 * 实现MethodInterceptor接口，在方法执行前后进行拦截
 * 负责在方法调用前将@DataPermission注解信息存入ThreadLocal
 * 在方法调用后清除ThreadLocal中的数据，防止内存泄漏
 *
 * 工作流程：
 * 1. 方法调用前：从方法或类上获取@DataPermission注解，存入ThreadLocal
 * 2. 执行目标方法：MyBatis-Plus拦截器会从ThreadLocal读取注解信息
 * 3. 方法调用后：清除ThreadLocal中的数据
 *
 * @author 秋辞未寒
 */
// Lombok日志注解：自动生成名为log的SLF4J日志对象
// 用于记录数据权限相关的日志信息
@Slf4j
public class DataPermissionAdvice implements MethodInterceptor {

    /**
     * 拦截方法调用，实现数据权限控制
     *
     * @param invocation 方法调用对象，包含目标对象、方法、参数等信息
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中抛出的异常
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // 获取目标对象（被代理的对象）
        // 对于MyBatis Mapper，这通常是Mapper接口的代理对象
        Object target = invocation.getThis();
        
        // 获取被调用的方法对象
        Method method = invocation.getMethod();
        
        // 获取方法调用参数
        Object[] args = invocation.getArguments();
        
        // 从方法或类上获取@DataPermission注解，并设置到ThreadLocal
        // DataPermissionHelper使用ThreadLocal存储当前线程的数据权限配置
        // 这样MyBatis-Plus拦截器可以获取到注解信息并生成SQL
        DataPermissionHelper.setPermission(getDataPermissionAnnotation(target, method, args));
        
        try {
            // 执行代理方法（调用实际的方法逻辑）
            // 在方法执行过程中，MyBatis-Plus拦截器会拦截SQL执行
            // 并从ThreadLocal读取数据权限配置，动态拼接SQL条件
            return invocation.proceed();
        } finally {
            // 方法执行完毕后，清除ThreadLocal中的数据权限配置
            // 防止内存泄漏和数据污染（非常重要）
            // 使用try-finally确保即使发生异常也能清除ThreadLocal
            DataPermissionHelper.removePermission();
        }
    }

    /**
     * 获取数据权限注解
     * 优先从方法上获取，如果方法上没有则从类上获取
     *
     * @param target 目标对象
     * @param method 方法对象
     * @param args 方法参数数组
     * @return DataPermission注解对象，如果没有则返回null
     */
    private DataPermission getDataPermissionAnnotation(Object target, Method method,Object[] args){
        // 优先获取方法上的@DataPermission注解
        // 方法级别的注解优先级高于类级别
        DataPermission dataPermission = method.getAnnotation(DataPermission.class);
        
        // 如果方法上有注解，直接返回
        if (dataPermission != null) {
            return dataPermission;
        }
        
        // 方法上没有注解，则尝试获取类上的注解
        Class<?> targetClass = target.getClass();
        
        // 判断是否是JDK动态代理类
        // MyBatis的Mapper接口就是通过JDK动态代理实现的
        if (Proxy.isProxyClass(targetClass)) {
            // 如果是代理类，获取其代理的接口（实际的Mapper接口）
            // JDK动态代理的原理：实现接口并代理接口方法
            // 所以通过getInterfaces()[0]可以获取到原始的Mapper接口类
            targetClass = targetClass.getInterfaces()[0];
        }
        
        // 从类上获取@DataPermission注解
        dataPermission = targetClass.getAnnotation(DataPermission.class);
        
        // 返回获取到的注解（可能为null）
        return dataPermission;
    }
}

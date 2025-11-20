package org.dromara.common.mybatis.aspect;

// Lombok日志注解，自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 数据权限注解，用于标记需要数据权限控制的方法或类
import org.dromara.common.mybatis.annotation.DataPermission;
// Spring AOP静态方法匹配切点，用于定义哪些方法需要被拦截
import org.springframework.aop.support.StaticMethodMatcherPointcut;

// Java反射Method类
import java.lang.reflect.Method;
// Java反射Proxy类，用于判断是否为动态代理类
import java.lang.reflect.Proxy;

/**
 * 数据权限匹配切点类
 * 继承StaticMethodMatcherPointcut，用于定义哪些方法需要应用数据权限拦截
 *
 * 工作原理：
 * 1. 检查方法上是否有@DataPermission注解
 * 2. 如果方法上没有，则检查类上是否有注解
 * 3. 特别处理JDK动态代理类（MyBatis Mapper）
 *
 * 匹配规则：
 * - 方法上有注解：匹配成功
 * - 方法上无注解但类上有注解：匹配成功
 * - 方法上和类上都没有注解：匹配失败
 *
 * @author 秋辞未寒
 */
// Lombok日志注解：自动生成名为log的SLF4J日志对象
@Slf4j
// 压制所有警告，因为IDE可能会提示未使用的log变量
@SuppressWarnings("all")
public class DataPermissionPointcut extends StaticMethodMatcherPointcut {

    /**
     * 判断方法是否匹配切点规则（是否需要数据权限拦截）
     *
     * @param method 目标方法对象
     * @param targetClass 目标类对象
     * @return true: 匹配成功，需要拦截；false: 匹配失败，不需要拦截
     */
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        // 优先匹配方法上的@DataPermission注解
        // 数据权限注解不对继承生效，所以只检查当前方法是否有注解
        // 不再往上匹配父类或接口，避免不必要的性能开销
        if (method.isAnnotationPresent(DataPermission.class)) {
            // 方法上有注解，匹配成功
            return true;
        }

        // 方法上没有注解，尝试匹配类上的注解
        // 由于MyBatis的Mapper接口是通过JDK动态代理实现的
        // Spring IOC容器拿到的实际上是MyBatis代理后的Mapper对象
        // 因此需要特殊处理动态代理类
        
        Class<?> targetClassRef = targetClass;
        
        // 判断是否是JDK动态代理类
        if (Proxy.isProxyClass(targetClassRef)) {
            // 数据权限注解不对继承生效，但由于SpringIOC容器拿到的实际上是MyBatis代理过后的Mapper
            // 而targetClass.isAnnotationPresent实际匹配的是Proxy类的注解，不会查找被代理的Mapper接口
            // 所以这里不能用targetClass.isAnnotationPresent，需要使用targetClass.getInterfaces()[0].isAnnotationPresent
            // 原理：JDK动态代理本质上就是对接口进行实现然后对具体的接口实现做代理
            // 所以直接通过接口可以拿到实际的Mapper接口类
            targetClassRef = targetClass.getInterfaces()[0];
        }
        
        // 检查类上是否有@DataPermission注解
        return targetClassRef.isAnnotationPresent(DataPermission.class);
    }

}

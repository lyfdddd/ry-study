// Spring工具类，继承Hutool的SpringUtil并提供增强功能
// 提供Bean判断、AOP代理获取、虚拟线程判断等扩展方法
package org.dromara.common.core.utils;

// Hutool封装的Spring工具类，提供基础的Spring操作（如getBean、getApplicationContext等）
import cn.hutool.extra.spring.SpringUtil;
// Spring Bean未找到异常（当容器中不存在指定Bean时抛出）
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
// Spring Boot虚拟线程配置类（Project Loom，JDK 19+特性）
import org.springframework.boot.autoconfigure.thread.Threading;
// Spring应用上下文接口，提供容器访问能力
import org.springframework.context.ApplicationContext;
// Spring环境配置接口，用于访问应用配置（如application.yml）
import org.springframework.core.env.Environment;
// Spring组件注解，将类注册为Spring Bean
import org.springframework.stereotype.Component;

/**
 * Spring工具类
 * 继承Hutool的SpringUtil，提供增强的Spring容器操作能力
 * 包括Bean判断、AOP代理获取、虚拟线程判断等扩展功能
 *
 * @author Lion Li
 */
// Spring组件注解：将此类注册为Spring Bean，纳入Spring容器管理
@Component
// final修饰符：防止类被继承，确保工具类的单一性和稳定性
public final class SpringUtils extends SpringUtil {

    /**
     * 判断Spring容器中是否存在指定名称的Bean
     * 用于安全地检查Bean是否存在，避免直接调用getBean抛出NoSuchBeanDefinitionException异常
     *
     * @param name Bean名称（如"userService"、"sysUserMapper"）
     * @return true表示容器中存在该Bean，false表示不存在
     */
    // 判断Spring容器中是否存在指定名称的Bean
    // 用于避免NoSuchBeanDefinitionException异常，提升代码健壮性
    public static boolean containsBean(String name) {
        // 获取BeanFactory（Bean工厂）并调用containsBean方法判断
        // getBeanFactory()从父类SpringUtil继承，返回ConfigurableListableBeanFactory
        return getBeanFactory().containsBean(name);
    }

    /**
     * 判断指定名称的Bean是否为单例模式
     * 如果与给定名称相应的bean定义没有被找到，将会抛出NoSuchBeanDefinitionException异常
     *
     * @param name Bean名称
     * @return true表示是单例模式，false表示是原型模式（prototype）
     * @throws NoSuchBeanDefinitionException 如果Bean不存在则抛出此异常
     */
    // 判断指定名称的Bean是否为单例模式
    // 单例：整个应用只有一个实例，Spring容器启动时创建；原型：每次获取都创建新实例
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        // 获取BeanFactory并调用isSingleton方法判断作用域
        // Spring默认作用域为singleton（单例），可通过@Scope注解修改为prototype（原型）
        return getBeanFactory().isSingleton(name);
    }

    /**
     * 获取指定名称Bean的Class类型
     * 如果Bean不存在会抛出NoSuchBeanDefinitionException异常
     *
     * @param name Bean名称
     * @return 注册对象的Class类型（如UserService.class、SysUserMapper.class）
     * @throws NoSuchBeanDefinitionException 如果Bean不存在则抛出此异常
     */
    // 获取指定名称Bean的Class类型
    // 用于反射操作或类型判断，例如在通用DAO中根据Bean名称获取实体类型
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        // 获取BeanFactory并调用getType方法获取类型
        // 返回Bean定义中的目标类型，如果Bean是代理对象，返回代理接口或目标类
        return getBeanFactory().getType(name);
    }

    /**
     * 获取指定Bean名称的所有别名
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名数组
     *
     * @param name Bean名称
     * @return 别名数组（一个Bean可以有多个别名，方便不同场景引用）
     * @throws NoSuchBeanDefinitionException 如果Bean不存在则抛出此异常
     */
    // 获取指定Bean名称的所有别名
    // 一个Bean可以有多个别名，方便不同场景引用（如userService和userServiceImpl指向同一Bean）
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        // 获取BeanFactory并调用getAliases方法获取别名数组
        // 别名可通过@Bean(name = {"alias1", "alias2"})或@Component("alias")指定
        return getBeanFactory().getAliases(name);
    }

    /**
     * 获取当前对象的AOP代理对象
     * 解决同类方法调用导致AOP切面失效的经典问题
     *
     * 问题场景：Service中方法A调用方法B，方法B上的@Transactional、@Cacheable等注解会失效
     * 原因：同类方法调用绕过了Spring AOP代理，直接调用目标对象的方法
     * 解决方案：使用getAopProxy(this).methodB()获取代理对象后调用，确保AOP切面生效
     *
     * @param invoker 当前对象（通常是this）
     * @param <T>     对象类型
     * @return AOP代理对象（如果类被代理）或原始对象
     */
    // 获取当前对象的AOP代理对象，解决同类方法调用导致AOP失效的问题
    // 例如：Service中方法A调用方法B，方法B上的@Transactional注解会失效
    // 使用getAopProxy(this).methodB()可以解决这个问题
    @SuppressWarnings("unchecked") // 抑制类型转换警告，因为getBean返回Object需要强制转换
    public static <T> T getAopProxy(T invoker) {
        // 从Spring容器中获取当前类的代理对象
        // getBean(Class)从父类继承，根据类型从容器中获取Bean
        // 如果该类被AOP代理（如@Transactional、@Cacheable），返回代理对象；否则返回原始对象
        return (T) getBean(invoker.getClass());
    }


    /**
     * 获取Spring应用上下文对象
     * 提供对ApplicationContext的访问能力，用于获取容器、发布事件等
     *
     * @return Spring应用上下文（ApplicationContext）
     */
    // 获取Spring应用上下文对象
    // 提供对ApplicationContext的访问能力，用于获取容器、发布事件等
    public static ApplicationContext context() {
        // 调用父类方法getApplicationContext()获取ApplicationContext
        // ApplicationContext是Spring容器的核心接口，提供Bean管理、事件发布、资源访问等功能
        return getApplicationContext();
    }

    /**
     * 判断当前是否启用虚拟线程（Project Loom）
     * 虚拟线程是JDK 19+引入的轻量级线程，可以创建数百万个线程而不耗尽系统资源
     * 相比平台线程（传统线程），虚拟线程的创建和切换成本极低
     *
     * @return true表示启用虚拟线程，false表示使用传统平台线程
     */
    // 判断当前是否启用虚拟线程（Project Loom）
    // 虚拟线程是JDK 19+的特性，可以创建大量轻量级线程
    public static boolean isVirtual() {
        // 从Spring环境中获取Threading配置，判断是否激活虚拟线程
        // Threading.VIRTUAL.isActive()检查spring.threads.virtual.enabled配置是否为true
        // 需要JDK 19+和Spring Boot 3.2+支持
        return Threading.VIRTUAL.isActive(getBean(Environment.class));
    }

}

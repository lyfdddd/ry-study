package org.dromara.common.mybatis.aspect;

// AOP联盟通知接口，定义拦截器要执行的通知逻辑
import org.aopalliance.aop.Advice;
// Spring AOP切点接口，定义哪些方法需要被拦截
import org.springframework.aop.Pointcut;
// Spring AOP抽象切点通知器，组合切点和通知
import org.springframework.aop.support.AbstractPointcutAdvisor;

/**
 * 数据权限切面定义类
 * 继承AbstractPointcutAdvisor，组合切点（Pointcut）和通知（Advice）
 * 用于将数据权限拦截逻辑注册到Spring AOP框架
 *
 * 职责：
 * 1. 定义切点：哪些方法需要被拦截（DataPermissionPointcut）
 * 2. 定义通知：拦截后要执行的逻辑（DataPermissionAdvice）
 * 3. 注册到Spring AOP：通过@Bean方式注册为Spring Bean
 *
 * @author 秋辞未寒
 */
// 压制所有警告，因为IDE可能会提示未使用的字段或方法
@SuppressWarnings("all")
public class DataPermissionPointcutAdvisor extends AbstractPointcutAdvisor {

    /**
     * 通知对象（拦截器）
     * 包含具体的拦截逻辑，在方法执行前后进行处理
     */
    private final Advice advice;
    
    /**
     * 切点对象
     * 定义哪些方法需要被拦截（匹配规则）
     */
    private final Pointcut pointcut;

    /**
     * 构造函数
     * 初始化通知和切点对象
     */
    public DataPermissionPointcutAdvisor() {
        // 创建数据权限通知对象，包含具体的拦截逻辑
        // DataPermissionAdvice会在方法执行前后处理数据权限
        this.advice = new DataPermissionAdvice();
        
        // 创建数据权限切点对象，定义匹配规则
        // DataPermissionPointcut会判断方法是否需要数据权限拦截
        this.pointcut = new DataPermissionPointcut();
    }

    /**
     * 获取切点对象
     *
     * @return 切点对象，定义哪些方法需要被拦截
     */
    @Override
    public Pointcut getPointcut() {
        // 返回切点对象，Spring AOP会使用它来判断是否拦截方法
        return this.pointcut;
    }

    /**
     * 获取通知对象
     *
     * @return 通知对象，包含拦截后的处理逻辑
     */
    @Override
    public Advice getAdvice() {
        // 返回通知对象，Spring AOP会在拦截到方法时执行它
        return this.advice;
    }

}

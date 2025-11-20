// 定义限流切面类的包路径
package org.dromara.common.ratelimiter.aspectj;

// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// AOP连接点接口
import org.aspectj.lang.JoinPoint;
// AOP切面注解
import org.aspectj.lang.annotation.Aspect;
// AOP前置通知注解
import org.aspectj.lang.annotation.Before;
// 方法签名接口
import org.aspectj.lang.reflect.MethodSignature;
// 全局常量
import org.dromara.common.core.constant.GlobalConstants;
// 服务异常类
import org.dromara.common.core.exception.ServiceException;
// 消息工具类，支持国际化
import org.dromara.common.core.utils.MessageUtils;
// Servlet工具类
import org.dromara.common.core.utils.ServletUtils;
// Spring工具类
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 限流注解
import org.dromara.common.ratelimiter.annotation.RateLimiter;
// 限流类型枚举
import org.dromara.common.ratelimiter.enums.LimitType;
// Redis工具类
import org.dromara.common.redis.utils.RedisUtils;
// Redisson限流类型枚举
import org.redisson.api.RateType;
// Spring Bean工厂解析器
import org.springframework.context.expression.BeanFactoryResolver;
// 基于方法的SpEL评估上下文
import org.springframework.context.expression.MethodBasedEvaluationContext;
// 默认参数名发现器
import org.springframework.core.DefaultParameterNameDiscoverer;
// 参数名发现器接口
import org.springframework.core.ParameterNameDiscoverer;
// SpEL表达式接口
import org.springframework.expression.Expression;
// SpEL表达式解析器接口
import org.springframework.expression.ExpressionParser;
// SpEL解析上下文接口
import org.springframework.expression.ParserContext;
// 模板解析上下文
import org.springframework.expression.common.TemplateParserContext;
// SpEL标准表达式解析器
import org.springframework.expression.spel.standard.SpelExpressionParser;

// Java反射Method类
import java.lang.reflect.Method;

/**
 * 限流处理切面类
 * 使用AOP拦截标记了@RateLimiter注解的方法，实现接口限流功能
 * 支持基于IP、集群实例和默认策略的限流
 * 使用Redisson的RateLimiter实现分布式限流
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成slf4j日志对象
@Slf4j
// AOP切面注解，声明这是一个切面类
@Aspect
// 限流切面类
public class RateLimiterAspect {

    /**
     * SpEL表达式解析器
     * 用于解析@RateLimiter注解中的key表达式
     */
    private final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * SpEL解析模板上下文
     * 定义表达式前缀和后缀，默认为#{}
     */
    private final ParserContext parserContext = new TemplateParserContext();
    
    /**
     * 方法参数名发现器
     * 用于获取方法参数名称，支持SpEL表达式引用参数
     */
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();

    /**
     * 前置通知处理方法
     * 在目标方法执行前进行限流检查
     *
     * @param point 连接点，包含目标方法信息
     * @param rateLimiter 限流注解配置
     */
    // AOP前置通知，拦截所有标记了@RateLimiter注解的方法
    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        // 获取限流时间窗口（秒）
        int time = rateLimiter.time();
        // 获取限流次数
        int count = rateLimiter.count();
        // 获取限流策略超时时间（秒）
        int timeout = rateLimiter.timeout();
        try {
            // 生成限流缓存key
            String combineKey = getCombineKey(rateLimiter, point);
            // 默认限流类型为全局
            RateType rateType = RateType.OVERALL;
            // 如果限流类型为集群，则使用PER_CLIENT模式
            if (rateLimiter.limitType() == LimitType.CLUSTER) {
                rateType = RateType.PER_CLIENT;
            }
            // 调用Redis限流工具，返回剩余令牌数
            long number = RedisUtils.rateLimiter(combineKey, rateType, count, time, timeout);
            // 如果返回-1，表示已触发限流
            if (number == -1) {
                // 获取提示消息
                String message = rateLimiter.message();
                // 如果消息是国际化格式（如{rate.limiter.message}），则进行国际化转换
                if (StringUtils.startsWith(message, "{") && StringUtils.endsWith(message, "}")) {
                    message = MessageUtils.message(StringUtils.substring(message, 1, message.length() - 1));
                }
                // 抛出服务异常，提示用户触发限流
                throw new ServiceException(message);
            }
            // 记录日志，显示限制令牌数、剩余令牌数和缓存key
            log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}'", count, number, combineKey);
        } catch (Exception e) {
            // 如果是服务异常（已触发限流），直接抛出
            if (e instanceof ServiceException) {
                throw e;
            } else {
                // 其他异常包装为运行时异常
                throw new RuntimeException("服务器限流异常，请稍候再试", e);
            }
        }
    }

    /**
     * 生成限流缓存key
     * 根据限流类型和SpEL表达式生成唯一的限流key
     *
     * @param rateLimiter 限流注解配置
     * @param point 连接点
     * @return 组合后的限流key
     */
    private String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        // 获取注解中配置的key
        String key = rateLimiter.key();
        // 判断key不为空且包含#（SpEL表达式标记）
        if (StringUtils.isNotBlank(key) && StringUtils.containsAny(key, "#")) {
            // 获取方法签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            // 获取目标方法
            Method targetMethod = signature.getMethod();
            // 获取方法参数值
            Object[] args = point.getArgs();
            // 创建基于方法的SpEL评估上下文
            MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(null, targetMethod, args, pnd);
            // 设置Bean解析器，支持引用Spring Bean
            context.setBeanResolver(new BeanFactoryResolver(SpringUtils.getBeanFactory()));
            // SpEL表达式对象
            Expression expression;
            // 如果key符合模板格式（如#{#code}），使用模板解析
            if (StringUtils.startsWith(key, parserContext.getExpressionPrefix())
                && StringUtils.endsWith(key, parserContext.getExpressionSuffix())) {
                expression = parser.parseExpression(key, parserContext);
            } else {
                // 否则直接解析
                expression = parser.parseExpression(key);
            }
            // 计算表达式值，获取最终的key字符串
            key = expression.getValue(context, String.class);
        }
        // 使用StringBuilder构建限流key
        StringBuilder stringBuffer = new StringBuilder(GlobalConstants.RATE_LIMIT_KEY);
        // 追加请求URI
        stringBuffer.append(ServletUtils.getRequest().getRequestURI()).append(":");
        // 根据限流类型追加不同标识
        if (rateLimiter.limitType() == LimitType.IP) {
            // IP限流：追加客户端IP地址
            stringBuffer.append(ServletUtils.getClientIP()).append(":");
        } else if (rateLimiter.limitType() == LimitType.CLUSTER) {
            // 集群限流：追加Redis客户端ID
            stringBuffer.append(RedisUtils.getClient().getId()).append(":");
        }
        // 追加最终的key值
        return stringBuffer.append(key).toString();
    }
}

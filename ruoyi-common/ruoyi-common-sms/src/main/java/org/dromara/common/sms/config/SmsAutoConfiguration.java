// 定义短信自动配置类的包路径
package org.dromara.common.sms.config;

// 引入PlusSmsDao实现类
import org.dromara.common.sms.core.dao.PlusSmsDao;
// 引入短信异常处理器
import org.dromara.common.sms.handler.SmsExceptionHandler;
// 引入Sms4j框架的SmsDao接口
import org.dromara.sms4j.api.dao.SmsDao;
// Spring Boot自动配置注解
import org.springframework.boot.autoconfigure.AutoConfiguration;
// Redis自动配置类，确保在Redis配置完成后加载
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
// Spring Bean注解
import org.springframework.context.annotation.Bean;
// Spring主Bean注解，当有多个同类型Bean时优先使用
import org.springframework.context.annotation.Primary;

/**
 * 短信自动配置类
 * 配置短信相关的Bean，包括SmsDao和异常处理器
 * 在Redis自动配置完成后加载，确保Redis可用
 *
 * @author Feng
 */
// Spring Boot自动配置注解，after属性指定在RedisAutoConfiguration之后加载
@AutoConfiguration(after = {RedisAutoConfiguration.class})
// 短信自动配置类
public class SmsAutoConfiguration {

    /**
     * 配置SmsDao Bean
     * 使用@Primary注解确保优先使用此实现
     * 返回PlusSmsDao实例，基于Redis实现短信缓存
     *
     * @return SmsDao实例
     */
    @Primary
    @Bean
    public SmsDao smsDao() {
        // 创建并返回PlusSmsDao实例
        return new PlusSmsDao();
    }

    /**
     * 配置短信异常处理器Bean
     * 用于全局捕获和处理短信发送异常
     *
     * @return SmsExceptionHandler实例
     */
    @Bean
    public SmsExceptionHandler smsExceptionHandler() {
        // 创建并返回SmsExceptionHandler实例
        return new SmsExceptionHandler();
    }

}

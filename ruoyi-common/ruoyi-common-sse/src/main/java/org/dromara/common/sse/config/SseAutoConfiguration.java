package org.dromara.common.sse.config;

import org.dromara.common.sse.controller.SseController;
import org.dromara.common.sse.core.SseEmitterManager;
import org.dromara.common.sse.listener.SseTopicListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * SSE 自动装配
 *
 * @author Lion Li
 */
// Spring Boot自动配置类，当sse.enabled=true时生效
@AutoConfiguration
// 条件注解，当配置项sse.enabled的值为true时才加载此配置类
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
// 启用配置属性绑定，将sse开头的配置项绑定到SseProperties类
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    // 创建SseEmitterManager Bean，用于管理SSE连接的生命周期
    @Bean
    public SseEmitterManager sseEmitterManager() {
        return new SseEmitterManager();
    }

    // 创建SseTopicListener Bean，用于监听Redis主题消息
    @Bean
    public SseTopicListener sseTopicListener() {
        return new SseTopicListener();
    }

    // 创建SseController Bean，提供SSE连接的REST接口
    @Bean
    public SseController sseController(SseEmitterManager sseEmitterManager) {
        return new SseController(sseEmitterManager);
    }

}

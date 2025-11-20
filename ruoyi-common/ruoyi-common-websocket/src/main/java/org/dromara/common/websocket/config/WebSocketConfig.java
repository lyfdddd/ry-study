// 定义WebSocket配置类所在的包路径
package org.dromara.common.websocket.config;

// 导入Hutool工具类中的字符串工具，用于判断字符串是否为空
import cn.hutool.core.util.StrUtil;
// 导入WebSocket配置属性类，用于读取配置文件中的WebSocket相关配置
import org.dromara.common.websocket.config.properties.WebSocketProperties;
// 导入WebSocket处理器类，用于处理WebSocket消息和连接事件
import org.dromara.common.websocket.handler.PlusWebSocketHandler;
// 导入WebSocket握手拦截器类，用于在握手阶段进行身份验证
import org.dromara.common.websocket.interceptor.PlusWebSocketInterceptor;
// 导入WebSocket主题监听器类，用于订阅Redis消息主题
import org.dromara.common.websocket.listener.WebSocketTopicListener;
// 导入Spring Boot自动配置注解，标识这是一个自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入条件注解，当配置文件中websocket.enabled=true时才加载此配置
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// 导入配置属性启用注解，使WebSocketProperties配置类生效
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// 导入Bean定义注解，用于在Spring容器中注册Bean
import org.springframework.context.annotation.Bean;
// 导入WebSocket处理器接口
import org.springframework.web.socket.WebSocketHandler;
// 导入启用WebSocket注解，开启WebSocket支持
import org.springframework.web.socket.config.annotation.EnableWebSocket;
// 导入WebSocket配置器接口，用于配置WebSocket端点
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
// 导入握手拦截器接口，用于拦截WebSocket握手请求
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 配置
 *
 * @author zendwang
 */
// Spring Boot自动配置类注解，表示这是一个自动配置类，会在Spring Boot启动时自动加载
@AutoConfiguration
// 条件注解，只有当配置文件中websocket.enabled=true时才会创建此配置类的Bean
@ConditionalOnProperty(value = "websocket.enabled", havingValue = "true")
// 启用配置属性功能，使WebSocketProperties类可以从配置文件中读取属性
@EnableConfigurationProperties(WebSocketProperties.class)
// 启用WebSocket功能，开启WebSocket支持
@EnableWebSocket
// WebSocket配置类，用于配置WebSocket端点、处理器和拦截器
public class WebSocketConfig {

    // 定义WebSocketConfigurer Bean，用于配置WebSocket端点
    @Bean
    // 方法参数：handshakeInterceptor（握手拦截器）、webSocketHandler（WebSocket处理器）、webSocketProperties（WebSocket配置属性）
    public WebSocketConfigurer webSocketConfigurer(HandshakeInterceptor handshakeInterceptor,
                                                   WebSocketHandler webSocketHandler, WebSocketProperties webSocketProperties) {
        // 使用Hutool的StrUtil判断配置的路径是否为空，如果为空则设置默认路径为"/websocket"
        // 这是为了提供默认的WebSocket连接路径，避免用户未配置时无法连接
        if (StrUtil.isBlank(webSocketProperties.getPath())) {
            webSocketProperties.setPath("/websocket");
        }

        // 判断配置的允许跨域地址是否为空，如果为空则设置为"*"，表示允许所有来源的跨域请求
        // "*"是通配符，允许任何域名访问WebSocket服务，方便前后端分离开发
        if (StrUtil.isBlank(webSocketProperties.getAllowedOrigins())) {
            webSocketProperties.setAllowedOrigins("*");
        }

        // 返回一个Lambda表达式实现的WebSocketConfigurer对象
        // 使用函数式编程方式配置WebSocket端点，简化代码
        return registry -> registry
            // 添加WebSocket处理器到指定路径，处理器负责处理消息和连接事件
            .addHandler(webSocketHandler, webSocketProperties.getPath())
            // 添加握手拦截器，在握手阶段进行身份验证和参数检查
            .addInterceptors(handshakeInterceptor)
            // 设置允许的跨域来源，解决跨域访问问题
            .setAllowedOrigins(webSocketProperties.getAllowedOrigins());
    }

    // 定义握手拦截器Bean，用于在WebSocket握手阶段进行拦截处理
    @Bean
    // 创建PlusWebSocketInterceptor实例，实现握手前后的逻辑
    public HandshakeInterceptor handshakeInterceptor() {
        return new PlusWebSocketInterceptor();
    }

    // 定义WebSocket处理器Bean，用于处理WebSocket消息和连接事件
    @Bean
    // 创建PlusWebSocketHandler实例，实现消息接收、连接管理等功能
    public WebSocketHandler webSocketHandler() {
        return new PlusWebSocketHandler();
    }

    // 定义WebSocket主题监听器Bean，用于订阅Redis消息主题
    @Bean
    // 创建WebSocketTopicListener实例，实现集群消息广播功能
    public WebSocketTopicListener topicListener() {
        return new WebSocketTopicListener();
    }
}

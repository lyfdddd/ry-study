// 定义WebSocket配置属性类所在的包路径
package org.dromara.common.websocket.config.properties;

// 导入Lombok的@Data注解，自动生成getter、setter、toString等方法
import lombok.Data;
// 导入Spring Boot配置属性注解，将配置文件中的websocket前缀属性映射到此类
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置项
 * 用于从application.yml或application.properties文件中读取WebSocket相关配置
 *
 * @author zendwang
 */
// 指定配置前缀为"websocket"，会自动绑定websocket.enabled、websocket.path等属性
@ConfigurationProperties("websocket")
// Lombok注解，自动生成getter、setter、equals、hashCode、toString方法，减少样板代码
@Data
// WebSocket配置属性类，存储WebSocket相关的配置信息
public class WebSocketProperties {

    /**
     * WebSocket功能开关，true表示启用，false表示禁用
     * 与@ConditionalOnProperty配合使用，控制是否加载WebSocket配置
     */
    private Boolean enabled;

    /**
     * WebSocket服务端点路径，客户端连接时使用此路径
     * 例如：ws://localhost:8080/websocket
     */
    private String path;

    /**
     * 设置访问源地址（CORS跨域配置）
     * "*"表示允许所有来源的跨域请求，也可以指定具体域名如"http://localhost:3000"
     */
    private String allowedOrigins;
}

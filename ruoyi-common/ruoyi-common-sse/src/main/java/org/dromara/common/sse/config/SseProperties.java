package org.dromara.common.sse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSE 配置项
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Spring Boot配置属性注解，将配置文件中以sse开头的属性绑定到此类
@ConfigurationProperties("sse")
public class SseProperties {

    // SSE功能开关，true表示启用，false表示禁用
    private Boolean enabled;

    /**
     * SSE连接路径，客户端通过此路径建立SSE连接
     */
    private String path;
}

package org.dromara.common.web.config;

// 导入Undertow的默认字节缓冲区池，用于WebSocket缓冲区管理
import io.undertow.server.DefaultByteBufferPool;
// 导入不允许的方法处理器，用于禁用不安全的HTTP方法
import io.undertow.server.handlers.DisallowedMethodsHandler;
// 导入HTTP字符串工具类，用于创建HTTP方法常量
import io.undertow.util.HttpString;
// 导入WebSocket部署信息类，用于配置WebSocket相关参数
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
// 导入Spring工具类，用于检测是否启用虚拟线程
import org.dromara.common.core.utils.SpringUtils;
// 导入Spring Boot自动配置注解，标识此类为自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入Undertow Servlet Web服务器工厂类
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
// 导入Web服务器工厂自定义接口，用于自定义Web服务器配置
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
// 导入虚拟线程任务执行器，用于在虚拟线程模式下执行任务
import org.springframework.core.task.VirtualThreadTaskExecutor;

/**
 * Undertow Web服务器自定义配置类
 * 实现WebServerFactoryCustomizer接口，对Undertow服务器进行深度定制
 * 主要配置内容包括：WebSocket支持、虚拟线程池、禁用不安全HTTP方法
 *
 * @author Lion Li
 */
// @AutoConfiguration注解：标识此类为Spring Boot自动配置类，Spring Boot启动时会自动加载
@AutoConfiguration
// 实现WebServerFactoryCustomizer接口：用于自定义Web服务器工厂配置
// 泛型参数UndertowServletWebServerFactory表示针对Undertow服务器的定制
public class UndertowConfig implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    /**
     * 自定义Undertow Web服务器配置
     * <p>
     * 主要配置内容包括：
     * 1. 配置WebSocket部署信息，设置缓冲区池
     * 2. 在虚拟线程模式下使用虚拟线程池，提升并发性能
     * 3. 禁用不安全的HTTP方法（CONNECT、TRACE、TRACK），防止安全漏洞
     * </p>
     *
     * @param factory Undertow的Web服务器工厂对象，用于添加自定义配置
     */
    // 重写WebServerFactoryCustomizer接口的customize方法，自定义Undertow配置
    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        // 添加部署信息自定义器，对部署信息进行深度定制
        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            // 配置WebSocket部署信息，设置WebSocket使用的缓冲区池
            // 创建WebSocketDeploymentInfo对象，用于配置WebSocket相关参数
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            // 设置缓冲区池，使用直接内存（true），缓冲区大小为1024字节
            // 直接内存可以减少GC压力，提升性能
            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(true, 1024));
            // 将WebSocket部署信息添加到Servlet上下文属性中
            // 属性名称为"io.undertow.websockets.jsr.WebSocketDeploymentInfo"，Undertow会自动读取
            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);

            // 检测是否启用了虚拟线程（JDK 21+）
            // SpringUtils.isVirtual()方法会检查JDK版本和系统属性
            if (SpringUtils.isVirtual()) {
                // 如果启用了虚拟线程，创建虚拟线程池
                // VirtualThreadTaskExecutor是Spring对虚拟线程的封装
                // 线程池名称为"undertow-"，便于日志和监控识别
                VirtualThreadTaskExecutor executor = new VirtualThreadTaskExecutor("undertow-");
                // 设置虚拟线程池为Servlet的执行器，处理HTTP请求
                deploymentInfo.setExecutor(executor);
                // 设置虚拟线程池为异步执行器，处理异步Servlet请求
                deploymentInfo.setAsyncExecutor(executor);
                // 使用虚拟线程可以大幅提升并发性能，每个请求一个虚拟线程，轻量且高效
            }

            // 配置禁止某些不安全的HTTP方法，提升安全性
            // 使用addInitialHandlerChainWrapper添加处理器链包装器
            deploymentInfo.addInitialHandlerChainWrapper(handler -> {
                // 定义不允许的HTTP方法数组，包含三个不安全的方法：
                // 1. CONNECT方法：用于代理隧道，可能被滥用
                // 2. TRACE方法：用于回显服务器收到的请求，可能导致XSS攻击
                // 3. TRACK方法：类似TRACE，也是调试方法，存在安全风险
                HttpString[] disallowedHttpMethods = {
                    // HttpString.tryFromString：将字符串转换为HTTP方法常量
                    HttpString.tryFromString("CONNECT"), // CONNECT方法，用于代理隧道
                    HttpString.tryFromString("TRACE"), // TRACE方法，用于回显服务器收到的请求
                    HttpString.tryFromString("TRACK") // TRACK方法，类似TRACE
                };
                // 使用DisallowedMethodsHandler包装原始处理器
                // 当请求使用不允许的方法时，会返回405 Method Not Allowed错误
                // 有效防止爬虫和恶意请求骚扰
                return new DisallowedMethodsHandler(handler, disallowedHttpMethods);
            });
        });
    }

}

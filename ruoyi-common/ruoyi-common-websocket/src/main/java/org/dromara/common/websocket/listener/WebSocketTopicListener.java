// 定义WebSocket主题监听器类所在的包路径
package org.dromara.common.websocket.listener;

// 导入Hutool的集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// 导入Lombok的日志注解
import lombok.extern.slf4j.Slf4j;
// 导入WebSocket会话持有器类
import org.dromara.common.websocket.holder.WebSocketSessionHolder;
// 导入WebSocket工具类
import org.dromara.common.websocket.utils.WebSocketUtils;
// 导入Spring Boot应用参数接口
import org.springframework.boot.ApplicationArguments;
// 导入Spring Boot应用运行器接口
import org.springframework.boot.ApplicationRunner;
// 导入Spring排序接口
import org.springframework.core.Ordered;

/**
 * WebSocket 主题订阅监听器
 * 实现ApplicationRunner接口，在Spring Boot启动完成后自动执行
 * 实现Ordered接口，控制初始化顺序（值越小优先级越高）
 * 负责订阅Redis消息主题，实现WebSocket消息的集群广播功能
 *
 * @author zendwang
 */
// Lombok日志注解，自动生成slf4j的log对象
@Slf4j
// WebSocket主题订阅监听器，在应用启动时订阅Redis主题
public class WebSocketTopicListener implements ApplicationRunner, Ordered {

    /**
     * 在Spring Boot应用程序启动完成后初始化WebSocket主题订阅监听器
     * 订阅Redis的WEB_SOCKET_TOPIC主题，接收其他服务实例发送的WebSocket消息
     *
     * @param args 应用程序参数，包含启动时的命令行参数
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 调用WebSocket工具类订阅Redis消息主题
        // 传入Lambda表达式作为消息处理器，处理接收到的消息
        WebSocketUtils.subscribeMessage((message) -> {
            // 记录日志，显示接收到的消息目标会话key列表和消息内容
            log.info("WebSocket主题订阅收到消息session keys={} message={}", message.getSessionKeys(), message.getMessage());
            
            // 判断消息的目标会话key列表是否不为空
            // 如果key不为空就按照key列表发送消息，如果为空就群发给所有在线用户
            if (CollUtil.isNotEmpty(message.getSessionKeys())) {
                // 遍历目标会话key列表（用户ID列表）
                message.getSessionKeys().forEach(key -> {
                    // 检查该用户是否在当前服务实例的会话持有器中（是否在线）
                    if (WebSocketSessionHolder.existSession(key)) {
                        // 如果用户在线，调用WebSocket工具类发送消息给该用户
                        WebSocketUtils.sendMessage(key, message.getMessage());
                    }
                });
            } else {
                // 如果消息的目标会话key列表为空，表示是群发消息
                // 获取所有在线用户的会话key集合（用户ID集合）
                WebSocketSessionHolder.getSessionsAll().forEach(key -> {
                    // 遍历所有在线用户，发送消息给每个用户
                    WebSocketUtils.sendMessage(key, message.getMessage());
                });
            }
        });
        // 记录日志，提示WebSocket主题订阅监听器初始化成功
        log.info("初始化WebSocket主题订阅监听器成功");
    }

    /**
     * 获取初始化顺序值
     * 值越小优先级越高，-1表示最高优先级，确保在应用启动早期就完成订阅
     *
     * @return 初始化顺序值，返回-1表示最高优先级
     */
    @Override
    public int getOrder() {
        // 返回-1，确保在应用启动时优先初始化WebSocket订阅
        return -1;
    }
}

package org.dromara.common.sse.listener;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.sse.core.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * SSE 主题订阅监听器
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成log日志对象
@Slf4j
public class SseTopicListener implements ApplicationRunner, Ordered {

    // Spring自动注入SseEmitterManager实例
    @Autowired
    private SseEmitterManager sseEmitterManager;

    /**
     * 在Spring Boot应用程序启动时初始化SSE主题订阅监听器
     *
     * @param args 应用程序参数
     * @throws Exception 初始化过程中可能抛出的异常
     */
    // 实现ApplicationRunner接口，在Spring Boot启动完成后执行
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 订阅Redis的SSE主题，接收消息并处理
        sseEmitterManager.subscribeMessage((message) -> {
            // 记录接收到的消息日志，包含用户ID列表和消息内容
            log.info("SSE主题订阅收到消息session keys={} message={}", message.getUserIds(), message.getMessage());
            // 如果key不为空就按照key发消息 如果为空就群发
            if (CollUtil.isNotEmpty(message.getUserIds())) {
                // 遍历用户ID列表，向每个用户发送消息
                message.getUserIds().forEach(key -> {
                    sseEmitterManager.sendMessage(key, message.getMessage());
                });
            } else {
                // 用户ID列表为空，向所有用户群发消息
                sseEmitterManager.sendMessage(message.getMessage());
            }
        });
        // 记录初始化成功日志
        log.info("初始化SSE主题订阅监听器成功");
    }

    // 实现Ordered接口，设置执行顺序为-1（最高优先级）
    @Override
    public int getOrder() {
        return -1;
    }
}

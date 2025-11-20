package org.dromara.common.sse.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.sse.core.SseEmitterManager;
import org.dromara.common.sse.dto.SseMessageDto;

/**
 * SSE工具类
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成log日志对象
@Slf4j
// Lombok注解，生成私有构造函数，防止外部实例化（工具类设计模式）
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessageUtils {

    // 从配置文件中读取sse.enabled属性，默认为true
    private final static Boolean SSE_ENABLE = SpringUtils.getProperty("sse.enabled", Boolean.class, true);
    // 静态变量，存储SseEmitterManager单例
    private static SseEmitterManager MANAGER;

    // 静态代码块，在类加载时初始化MANAGER
    static {
        // 如果SSE功能启用且MANAGER未初始化，则从Spring容器中获取
        if (isEnable() && MANAGER == null) {
            MANAGER = SpringUtils.getBean(SseEmitterManager.class);
        }
    }

    /**
     * 向指定的SSE会话发送消息
     *
     * @param userId  要发送消息的用户id
     * @param message 要发送的消息内容
     */
    public static void sendMessage(Long userId, String message) {
        // 如果SSE功能未启用，直接返回
        if (!isEnable()) {
            return;
        }
        // 调用SseEmitterManager发送消息
        MANAGER.sendMessage(userId, message);
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param message 要发送的消息内容
     */
    public static void sendMessage(String message) {
        // 如果SSE功能未启用，直接返回
        if (!isEnable()) {
            return;
        }
        // 调用SseEmitterManager群发消息
        MANAGER.sendMessage(message);
    }

    /**
     * 发布SSE订阅消息
     *
     * @param sseMessageDto 要发布的SSE消息对象
     */
    public static void publishMessage(SseMessageDto sseMessageDto) {
        // 如果SSE功能未启用，直接返回
        if (!isEnable()) {
            return;
        }
        // 调用SseEmitterManager发布消息到Redis主题
        MANAGER.publishMessage(sseMessageDto);
    }

    /**
     * 向所有的用户发布订阅的消息(群发)
     *
     * @param message 要发布的消息内容
     */
    public static void publishAll(String message) {
        // 如果SSE功能未启用，直接返回
        if (!isEnable()) {
            return;
        }
        // 调用SseEmitterManager发布群发消息到Redis主题
        MANAGER.publishAll(message);
    }

    /**
     * 是否开启
     */
    public static Boolean isEnable() {
        return SSE_ENABLE;
    }

}

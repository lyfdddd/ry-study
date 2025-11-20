package org.dromara.common.sse.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.sse.core.SseEmitterManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 控制器
 *
 * @author Lion Li
 */
// Spring MVC的REST控制器注解，处理HTTP请求并返回JSON响应
@RestController
// 条件注解，当配置项sse.enabled的值为true时才加载此控制器
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    // 注入SseEmitterManager，用于管理SSE连接的生命周期
    private final SseEmitterManager sseEmitterManager;

    /**
     * 建立 SSE 连接
     */
    // 处理GET请求，路径从配置文件中读取，返回类型为text/event-stream（SSE标准格式）
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        // 检查用户是否已登录，未登录返回null（不建立连接）
        if (!StpUtil.isLogin()) {
            return null;
        }
        // 获取当前用户的Token值，用于标识具体连接
        String tokenValue = StpUtil.getTokenValue();
        // 获取当前用户ID
        Long userId = LoginHelper.getUserId();
        // 调用SseEmitterManager建立连接
        return sseEmitterManager.connect(userId, tokenValue);
    }

    /**
     * 关闭 SSE 连接
     */
    // Sa-Token注解，忽略此接口的鉴权（允许未登录访问）
    @SaIgnore
    // 处理GET请求，路径为/sse/close
    @GetMapping(value = "${sse.path}/close")
    public R<Void> close() {
        // 获取当前用户的Token值
        String tokenValue = StpUtil.getTokenValue();
        // 获取当前用户ID
        Long userId = LoginHelper.getUserId();
        // 调用SseEmitterManager断开连接
        sseEmitterManager.disconnect(userId, tokenValue);
        // 返回成功响应
        return R.ok();
    }

    // 以下为demo仅供参考 禁止使用 请在业务逻辑中使用工具发送而不是用接口发送
//    /**
//     * 向特定用户发送消息
//     *
//     * @param userId 目标用户的 ID
//     * @param msg    要发送的消息内容
//     */
//    @GetMapping(value = "${sse.path}/send")
//    public R<Void> send(Long userId, String msg) {
//        SseMessageDto dto = new SseMessageDto();
//        dto.setUserIds(List.of(userId));
//        dto.setMessage(msg);
//        sseEmitterManager.publishMessage(dto);
//        return R.ok();
//    }
//
//    /**
//     * 向所有用户发送消息
//     *
//     * @param msg 要发送的消息内容
//     */
//    @GetMapping(value = "${sse.path}/sendAll")
//    public R<Void> send(String msg) {
//        sseEmitterManager.publishAll(msg);
//        return R.ok();
//    }

    /**
     * 清理资源。此方法目前不执行任何操作，但避免因未实现而导致错误
     */
    // 实现DisposableBean接口的销毁方法，在Bean销毁时调用
    @Override
    public void destroy() throws Exception {
        // 销毁时不需要做什么 此方法避免无用操作报错
    }

}

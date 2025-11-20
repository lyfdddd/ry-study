// 定义WebSocket处理器类所在的包路径
package org.dromara.common.websocket.handler;

// 导入Hutool的对象工具类，用于判断对象是否为空
import cn.hutool.core.util.ObjectUtil;
// 导入Lombok的日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入登录用户模型类，存储用户登录信息
import org.dromara.common.core.domain.model.LoginUser;
// 导入WebSocket消息DTO类，用于封装消息数据
import org.dromara.common.websocket.dto.WebSocketMessageDto;
// 导入WebSocket会话持有器类，管理所有在线会话
import org.dromara.common.websocket.holder.WebSocketSessionHolder;
// 导入WebSocket工具类，提供消息发送等功能
import org.dromara.common.websocket.utils.WebSocketUtils;
// 导入Spring WebSocket相关类
import org.springframework.web.socket.*;
// 导入抽象WebSocket处理器基类
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

// 导入IO异常类
import java.io.IOException;
// 导入List集合接口
import java.util.List;

// 静态导入WebSocket常量类中的登录用户key
import static org.dromara.common.websocket.constant.WebSocketConstants.LOGIN_USER_KEY;

/**
 * WebSocketHandler 实现类
 * 继承AbstractWebSocketHandler，处理WebSocket连接、消息、错误和关闭事件
 *
 * @author zendwang
 */
// Lombok日志注解，自动生成slf4j的log对象，用于记录日志
@Slf4j
// WebSocket处理器实现类，处理客户端连接和消息
public class PlusWebSocketHandler extends AbstractWebSocketHandler {

    /**
     * WebSocket连接建立成功后的回调方法
     * 在客户端成功连接后调用，将用户信息存储到会话中
     *
     * @param session WebSocket会话对象，包含连接信息和属性
     * @throws IOException 关闭会话时可能抛出的IO异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // 从会话属性中获取登录用户信息，key为LOGIN_USER_KEY
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER_KEY);
        
        // 使用Hutool的ObjectUtil判断登录用户是否为空，如果为空表示认证失败
        if (ObjectUtil.isNull(loginUser)) {
            // 关闭会话，状态码为BAD_DATA（错误数据），表示无效连接
            session.close(CloseStatus.BAD_DATA);
            // 记录日志，提示收到无效的token
            log.info("[connect] invalid token received. sessionId: {}", session.getId());
            // 直接返回，不继续处理
            return;
        }
        
        // 将WebSocket会话添加到会话持有器中，以用户ID为key
        WebSocketSessionHolder.addSession(loginUser.getUserId(), session);
        // 记录连接成功日志，包含会话ID、用户ID和用户类型
        log.info("[connect] sessionId: {},userId:{},userType:{}", session.getId(), loginUser.getUserId(), loginUser.getUserType());
    }

    /**
     * 处理接收到的文本消息
     * 当客户端发送文本消息时触发此方法
     *
     * @param session WebSocket会话对象
     * @param message 接收到的文本消息对象
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从会话属性中获取登录用户信息
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER_KEY);

        // 创建WebSocket消息DTO对象，用于封装消息数据
        WebSocketMessageDto webSocketMessageDto = new WebSocketMessageDto();
        // 设置消息目标会话key列表，这里设置为当前用户的ID（实现回声测试）
        webSocketMessageDto.setSessionKeys(List.of(loginUser.getUserId()));
        // 设置消息内容为接收到的消息负载（payload）
        webSocketMessageDto.setMessage(message.getPayload());
        // 调用WebSocket工具类发布消息，消息会发送到Redis主题实现集群广播
        WebSocketUtils.publishMessage(webSocketMessageDto);
    }

    /**
     * 处理接收到的二进制消息
     * 当客户端发送二进制消息（如图片、文件）时触发此方法
     *
     * @param session WebSocket会话对象
     * @param message 接收到的二进制消息对象
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 调用父类的handleBinaryMessage方法，使用默认处理逻辑
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的Pong消息（心跳监测）
     * 当客户端发送Pong消息响应心跳时触发此方法
     *
     * @param session WebSocket会话对象
     * @param message 接收到的Pong消息对象
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        // 调用WebSocket工具类向客户端发送Pong消息，维持心跳连接
        WebSocketUtils.sendPongMessage(session);
    }

    /**
     * 处理WebSocket传输错误
     * 当WebSocket传输过程中发生错误时触发此方法
     *
     * @param session   WebSocket会话对象
     * @param exception 发生的异常对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 记录传输错误日志，包含会话ID和异常信息
        log.error("[transport error] sessionId: {} , exception:{}", session.getId(), exception.getMessage());
    }

    /**
     * 在WebSocket连接关闭后执行清理操作
     * 当客户端断开连接或连接异常关闭时触发此方法
     *
     * @param session WebSocket会话对象
     * @param status  关闭状态信息，包含关闭原因和代码
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 从会话属性中获取登录用户信息
        LoginUser loginUser = (LoginUser) session.getAttributes().get(LOGIN_USER_KEY);
        
        // 判断登录用户是否为空，如果为空表示无效连接
        if (ObjectUtil.isNull(loginUser)) {
            // 记录日志，提示收到无效的token
            log.info("[disconnect] invalid token received. sessionId: {}", session.getId());
            // 直接返回，不继续处理
            return;
        }
        
        // 从会话持有器中移除该用户的会话，释放资源
        WebSocketSessionHolder.removeSession(loginUser.getUserId());
        // 记录断开连接日志，包含会话ID、用户ID和用户类型
        log.info("[disconnect] sessionId: {},userId:{},userType:{}", session.getId(), loginUser.getUserId(), loginUser.getUserType());
    }

    /**
     * 指示处理程序是否支持接收部分消息
     * 如果返回true，大消息会分多次接收；如果返回false，消息必须完整接收
     *
     * @return 如果支持接收部分消息，则返回true；否则返回false
     */
    @Override
    public boolean supportsPartialMessages() {
        // 返回false，表示不支持部分消息，必须完整接收消息
        return false;
    }

}

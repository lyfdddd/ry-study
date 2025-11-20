// 定义WebSocket常量接口所在的包路径
package org.dromara.common.websocket.constant;

/**
 * WebSocket的常量配置接口
 * 集中定义WebSocket相关的常量字符串，便于统一管理和维护
 *
 * @author zendwang
 */
// 定义为一个接口，用于存储常量，符合Java常量定义的最佳实践
public interface WebSocketConstants {

    /**
     * WebSocket会话中存储登录用户信息的key
     * 在握手阶段将LoginUser对象放入session attributes时使用此key
     */
    String LOGIN_USER_KEY = "loginUser";

    /**
     * Redis中WebSocket消息订阅的频道名称
     * 用于集群环境下广播WebSocket消息，实现多实例间的消息同步
     */
    String WEB_SOCKET_TOPIC = "global:websocket";

    /**
     * 前端发送的心跳检查命令字符串
     * 客户端定期发送"ping"来检测连接是否存活
     */
    String PING = "ping";

    /**
     * 服务端响应心跳的回复字符串
     * 收到"ping"后回复"pong"，表示连接正常
     */
    String PONG = "pong";
}

// 定义WebSocket会话持有器类所在的包路径
package org.dromara.common.websocket.holder;

// 导入Lombok的访问级别枚举
import lombok.AccessLevel;
// 导入Lombok的无参构造函数注解
import lombok.NoArgsConstructor;
// 导入Spring WebSocket的关闭状态枚举
import org.springframework.web.socket.CloseStatus;
// 导入Spring WebSocket的会话接口
import org.springframework.web.socket.WebSocketSession;

// 导入Map集合接口
import java.util.Map;
// 导入Set集合接口
import java.util.Set;
// 导入并发HashMap，线程安全的Map实现
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketSession 会话持有器
 * 用于保存当前所有在线的WebSocket会话信息，提供会话的增删改查操作
 * 使用ConcurrentHashMap保证线程安全，支持高并发场景
 *
 * @author zendwang
 */
// Lombok注解，生成私有无参构造函数，防止外部实例化，符合工具类设计模式
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// WebSocket会话持有器工具类，管理所有在线用户的WebSocket会话
public class WebSocketSessionHolder {

    /**
     * 用户会话映射表，key为用户ID，value为WebSocket会话对象
     * 使用ConcurrentHashMap保证线程安全，支持多线程并发访问
     */
    private static final Map<Long, WebSocketSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 将WebSocket会话添加到用户会话Map中
     * 如果该用户已存在会话，会先移除旧会话再添加新会话（保证单设备登录）
     *
     * @param sessionKey 会话键，通常为用户ID，用于检索会话
     * @param session    要添加的WebSocket会话对象
     */
    public static void addSession(Long sessionKey, WebSocketSession session) {
        // 先移除该用户已有的会话（如果有），避免重复登录
        removeSession(sessionKey);
        // 将新会话放入Map中，key为用户ID
        USER_SESSION_MAP.put(sessionKey, session);
    }

    /**
     * 从用户会话Map中移除指定会话键对应的WebSocket会话
     * 移除后会关闭会话连接，释放资源
     *
     * @param sessionKey 要移除的会话键（用户ID）
     */
    public static void removeSession(Long sessionKey) {
        // 从Map中移除会话，并返回被移除的会话对象
        WebSocketSession session = USER_SESSION_MAP.remove(sessionKey);
        // 判断会话是否不为空
        if (session != null) {
            try {
                // 关闭WebSocket会话，状态码为BAD_DATA
                session.close(CloseStatus.BAD_DATA);
            } catch (Exception ignored) {
                // 忽略关闭异常，防止影响主流程
            }
        }
    }

    /**
     * 根据会话键从用户会话Map中获取WebSocket会话
     *
     * @param sessionKey 要获取的会话键（用户ID）
     * @return 与给定会话键对应的WebSocket会话，如果不存在则返回null
     */
    public static WebSocketSession getSessions(Long sessionKey) {
        // 从ConcurrentHashMap中获取会话，线程安全
        return USER_SESSION_MAP.get(sessionKey);
    }

    /**
     * 获取存储在用户会话Map中所有WebSocket会话的会话键集合
     *
     * @return 所有WebSocket会话的会话键集合（所有在线用户ID）
     */
    public static Set<Long> getSessionsAll() {
        // 返回Map的key集合，即所有在线用户ID
        return USER_SESSION_MAP.keySet();
    }

    /**
     * 检查给定的会话键是否存在于用户会话Map中
     * 用于判断用户是否在线
     *
     * @param sessionKey 要检查的会话键（用户ID）
     * @return 如果存在对应的会话键（用户在线），则返回true；否则返回false
     */
    public static Boolean existSession(Long sessionKey) {
        // 调用ConcurrentHashMap的containsKey方法检查key是否存在
        return USER_SESSION_MAP.containsKey(sessionKey);
    }
}

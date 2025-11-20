// 定义WebSocket握手拦截器类所在的包路径
package org.dromara.common.websocket.interceptor;

// 导入Sa-Token的未登录异常类
import cn.dev33.satoken.exception.NotLoginException;
// 导入Sa-Token的StpUtil工具类，用于操作Token
import cn.dev33.satoken.stp.StpUtil;
// 导入Lombok的日志注解
import lombok.extern.slf4j.Slf4j;
// 导入登录用户模型类
import org.dromara.common.core.domain.model.LoginUser;
// 导入Servlet工具类，用于获取HTTP请求
import org.dromara.common.core.utils.ServletUtils;
// 导入字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 导入登录助手类，获取当前登录用户信息
import org.dromara.common.satoken.utils.LoginHelper;
// 导入Spring的HTTP请求接口
import org.springframework.http.server.ServerHttpRequest;
// 导入Spring的HTTP响应接口
import org.springframework.http.server.ServerHttpResponse;
// 导入WebSocket处理器接口
import org.springframework.web.socket.WebSocketHandler;
// 导入握手拦截器接口
import org.springframework.web.socket.server.HandshakeInterceptor;

// 导入Map集合接口
import java.util.Map;

// 静态导入WebSocket常量类中的登录用户key
import static org.dromara.common.websocket.constant.WebSocketConstants.LOGIN_USER_KEY;

/**
 * WebSocket握手请求的拦截器
 * 实现HandshakeInterceptor接口，在WebSocket握手阶段进行身份验证和参数校验
 * 确保只有合法用户才能建立WebSocket连接
 *
 * @author zendwang
 */
// Lombok日志注解，自动生成slf4j的log对象
@Slf4j
// WebSocket握手拦截器实现类，负责握手前后的逻辑处理
public class PlusWebSocketInterceptor implements HandshakeInterceptor {

    /**
     * WebSocket握手之前执行的前置处理方法
     * 在握手建立前进行身份验证和参数检查，决定是否允许建立连接
     *
     * @param request    WebSocket握手请求对象
     * @param response   WebSocket握手响应对象
     * @param wsHandler  WebSocket处理器对象
     * @param attributes 与WebSocket会话关联的属性Map，用于传递数据给后续处理器
     * @return 如果允许握手继续进行，则返回true；否则返回false
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            // 检查用户是否已登录，获取当前登录用户信息
            // LoginHelper.getLoginUser()会从Sa-Token中获取当前用户信息
            LoginUser loginUser = LoginHelper.getLoginUser();

            // 解决WebSocket不走Spring MVC拦截器的问题（cloud版本不受影响）
            // 检查header和param中的clientId与token中的clientId是否一致，防止token被盗用
            String headerCid = ServletUtils.getRequest().getHeader(LoginHelper.CLIENT_KEY);
            String paramCid = ServletUtils.getParameter(LoginHelper.CLIENT_KEY);
            // 从Sa-Token的扩展信息中获取clientId
            String clientId = StpUtil.getExtra(LoginHelper.CLIENT_KEY).toString();
            
            // 使用StringUtils的equalsAny方法比较三个clientId是否一致
            // 如果不一致，说明token可能被劫持或在不同客户端使用
            if (!StringUtils.equalsAny(clientId, headerCid, paramCid)) {
                // token无效，抛出未登录异常
                throw NotLoginException.newInstance(StpUtil.getLoginType(),
                    "-100", "客户端ID与Token不匹配",
                    StpUtil.getTokenValue());
            }

            // 将登录用户信息放入attributes中，key为LOGIN_USER_KEY
            // 这样在WebSocketHandler中可以通过session.getAttributes().get(LOGIN_USER_KEY)获取用户信息
            attributes.put(LOGIN_USER_KEY, loginUser);
            // 返回true，允许握手继续，建立WebSocket连接
            return true;
        } catch (NotLoginException e) {
            // 捕获未登录异常，记录错误日志
            log.error("WebSocket 认证失败'{}',无法访问系统资源", e.getMessage());
            // 返回false，拒绝握手，不建立WebSocket连接
            return false;
        }
    }

    /**
     * WebSocket握手成功后执行的后置处理方法
     * 在握手成功后调用，可以执行一些后续处理逻辑
     *
     * @param request   WebSocket握手请求对象
     * @param response  WebSocket握手响应对象
     * @param wsHandler WebSocket处理器对象
     * @param exception 握手过程中可能出现的异常，如果握手成功则为null
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 在这个方法中可以执行一些握手成功后的后续处理逻辑
        // 比如记录日志、初始化资源、发送欢迎消息等操作
        // 当前实现为空，表示不需要额外的后置处理
    }

}

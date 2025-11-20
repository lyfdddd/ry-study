package org.dromara.common.web.filter;

// 导入字符串工具类，用于字符串比较和判断
import org.dromara.common.core.utils.StringUtils;
// 导入Spring的MediaType常量，用于判断请求内容类型
import org.springframework.http.MediaType;

// 导入Servlet API相关类
import jakarta.servlet.*; // 导入过滤器相关接口和类
import jakarta.servlet.http.HttpServletRequest; // 导入HTTP请求接口
import java.io.IOException; // 导入IO异常类

/**
 * 可重复读取请求体的过滤器
 * 解决HTTP请求流只能读取一次的问题，对于JSON请求包装为RepeatedlyRequestWrapper
 * 使得请求体可以被多次读取（如AOP日志记录和业务处理都需要读取请求体）
 *
 * @author ruoyi
 */
public class RepeatableFilter implements Filter {
    /**
     * 过滤器初始化方法
     * 在过滤器创建时调用，可用于加载配置
     *
     * @param filterConfig 过滤器配置对象
     * @throws ServletException Servlet异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 当前实现为空，不需要初始化操作
    }

    /**
     * 过滤器核心处理方法
     * 判断请求是否为JSON类型，如果是则包装为可重复读取的RequestWrapper
     *
     * @param request  Servlet请求对象
     * @param response Servlet响应对象
     * @param chain    过滤器链
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        // 定义请求包装器变量，初始化为null
        ServletRequest requestWrapper = null;
        // 判断请求是否为HTTP请求且内容类型为JSON
        if (request instanceof HttpServletRequest
            && StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
            // 创建可重复读取的请求包装器，缓存请求体内容
            requestWrapper = new RepeatedlyRequestWrapper((HttpServletRequest) request, response);
        }
        // 如果不需要包装（非JSON请求），直接放行
        if (null == requestWrapper) {
            chain.doFilter(request, response);
        } else {
            // 使用包装后的请求对象继续过滤器链
            // 后续所有组件读取的都是包装器中的缓存内容
            chain.doFilter(requestWrapper, response);
        }
    }

    /**
     * 过滤器销毁方法
     * 在过滤器被移除时调用，可用于资源清理
     */
    @Override
    public void destroy() {
        // 当前实现为空，不需要清理资源
    }
}

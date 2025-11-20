package org.dromara.common.web.interceptor;

// 导入Hutool的IO工具类，提供流操作便捷方法
import cn.hutool.core.io.IoUtil;
// 导入Hutool的Map工具类，用于Map判空操作
import cn.hutool.core.map.MapUtil;
// 导入Hutool的对象工具类，用于对象判空操作
import cn.hutool.core.util.ObjectUtil;
// 导入HTTP请求接口
import jakarta.servlet.http.HttpServletRequest;
// 导入HTTP响应接口
import jakarta.servlet.http.HttpServletResponse;
// 导入Lombok的Slf4j注解，自动生成日志记录器
import lombok.extern.slf4j.Slf4j;
// 导入Apache Commons Lang3的StopWatch类，用于统计执行时间
import org.apache.commons.lang3.time.StopWatch;
// 导入字符串工具类，用于字符串判断
import org.dromara.common.core.utils.StringUtils;
// 导入JSON工具类，用于对象序列化为JSON字符串
import org.dromara.common.json.utils.JsonUtils;
// 导入可重复读取请求体的请求包装器
import org.dromara.common.web.filter.RepeatedlyRequestWrapper;
// 导入Spring的MediaType常量，用于判断请求内容类型
import org.springframework.http.MediaType;
// 导入Spring MVC的处理器拦截器接口
import org.springframework.web.servlet.HandlerInterceptor;
// 导入Spring MVC的模型和视图对象
import org.springframework.web.servlet.ModelAndView;

// 导入Java IO相关类
import java.io.BufferedReader; // 导入缓冲字符输入流
import java.util.Map; // 导入Map接口

/**
 * Web请求调用时间统计拦截器
 * 实现HandlerInterceptor接口，在请求处理前后记录日志和统计执行时间
 * 用于监控接口性能和调试
 *
 * @author Lion Li
 * @since 3.3.0
 */
// Lombok注解：自动生成日志记录器
@Slf4j
public class PlusWebInvokeTimeInterceptor implements HandlerInterceptor {

    /**
     * ThreadLocal缓存StopWatch对象
     * 使用ThreadLocal确保每个线程独立存储计时器，避免线程安全问题
     */
    private final static ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    /**
     * 请求预处理回调方法
     * 在Controller方法执行前调用，记录请求开始日志和参数信息
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器对象
     * @return true-继续执行后续拦截器和处理器，false-中断执行
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 构建请求URL字符串，格式：GET /api/user
        String url = request.getMethod() + " " + request.getRequestURI();

        // 打印请求参数信息
        // 判断是否为JSON请求
        if (isJsonRequest(request)) {
            // 初始化JSON参数字符串为空
            String jsonParam = "";
            // 判断请求是否为可重复读取包装器（确保可以多次读取请求体）
            if (request instanceof RepeatedlyRequestWrapper) {
                // 获取请求体读取器
                BufferedReader reader = request.getReader();
                // 使用Hutool的IoUtil读取所有内容
                jsonParam = IoUtil.read(reader);
            }
            // 记录JSON请求日志，包含URL和参数
            log.info("[PLUS]开始请求 => URL[{}],参数类型[json],参数:[{}]", url, jsonParam);
        } else {
            // 非JSON请求，获取请求参数Map
            Map<String, String[]> parameterMap = request.getParameterMap();
            // 判断参数Map是否不为空
            if (MapUtil.isNotEmpty(parameterMap)) {
                // 使用JsonUtils将参数Map序列化为JSON字符串
                String parameters = JsonUtils.toJsonString(parameterMap);
                // 记录普通参数请求日志，包含URL和参数
                log.info("[PLUS]开始请求 => URL[{}],参数类型[param],参数:[{}]", url, parameters);
            } else {
                // 记录无参数请求日志
                log.info("[PLUS]开始请求 => URL[{}],无参数", url);
            }
        }

        // 创建StopWatch计时器对象
        StopWatch stopWatch = new StopWatch();
        // 将计时器存入ThreadLocal，确保同线程内共享
        KEY_CACHE.set(stopWatch);
        // 启动计时器，开始记录方法执行时间
        stopWatch.start();

        // 返回true，继续执行后续拦截器和处理器
        return true;
    }

    /**
     * 请求处理完成后回调方法（视图渲染前）
     * 当前实现为空，不执行任何操作
     *
     * @param request      HTTP请求对象
     * @param response     HTTP响应对象
     * @param handler      处理器对象
     * @param modelAndView 模型和视图对象
     * @throws Exception 异常
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 当前实现为空，不执行任何操作
        // 该方法在Controller方法执行后、视图渲染前调用
    }

    /**
     * 请求完全处理完成后回调方法（视图渲染后）
     * 记录请求结束日志和耗时统计
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器对象
     * @param ex       异常对象（如果有）
     * @throws Exception 异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 从ThreadLocal获取StopWatch计时器
        StopWatch stopWatch = KEY_CACHE.get();
        // 判断计时器是否不为null
        if (ObjectUtil.isNotNull(stopWatch)) {
            // 停止计时器
            stopWatch.stop();
            // 记录请求结束日志，包含URL和耗时（毫秒）
            log.info("[PLUS]结束请求 => URL[{}],耗时:[{}]毫秒", request.getMethod() + " " + request.getRequestURI(), stopWatch.getDuration().toMillis());
            // 从ThreadLocal移除计时器，防止内存泄漏
            KEY_CACHE.remove();
        }
    }

    /**
     * 判断本次请求的数据类型是否为JSON
     * 检查Content-Type请求头是否以application/json开头
     *
     * @param request HTTP请求对象
     * @return true-是JSON请求，false-不是JSON请求
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        // 获取Content-Type请求头
        String contentType = request.getContentType();
        // 判断Content-Type是否不为null
        if (contentType != null) {
            // 使用StringUtils判断Content-Type是否以application/json开头（忽略大小写）
            return StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE);
        }
        // Content-Type为null，返回false
        return false;
    }

}

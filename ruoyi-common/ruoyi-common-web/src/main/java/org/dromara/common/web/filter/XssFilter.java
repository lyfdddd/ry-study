package org.dromara.common.web.filter;

// 导入Spring工具类，用于获取Spring容器中的Bean
// SpringUtils提供静态方法访问Spring容器，实现依赖查找
import org.dromara.common.core.utils.SpringUtils;
// 导入字符串工具类，用于URL匹配和判断
// StringUtils提供字符串操作工具方法，如matches、isEmpty等
import org.dromara.common.core.utils.StringUtils;
// 导入XSS防护配置属性类，获取排除URL配置
// XssProperties从application.yml中读取xss前缀的配置项，如enabled、excludeUrls
import org.dromara.common.web.config.properties.XssProperties;
// 导入Spring的HTTP方法枚举，用于判断请求方法
// HttpMethod是Spring提供的HTTP方法枚举，包含GET、POST、PUT、DELETE等
import org.springframework.http.HttpMethod;

// 导入Servlet API相关类
// Filter是Servlet规范定义的过滤器接口，用于拦截请求和响应
import jakarta.servlet.*; // 导入过滤器相关接口和类
// HttpServletRequest是Servlet规范定义的HTTP请求接口，提供获取请求信息的方法
import jakarta.servlet.http.HttpServletRequest; // 导入HTTP请求接口
// HttpServletResponse是Servlet规范定义的HTTP响应接口，提供设置响应信息的方法
import jakarta.servlet.http.HttpServletResponse; // 导入HTTP响应接口
// IOException是Java IO操作可能抛出的异常
import java.io.IOException; // 导入IO异常类
// ArrayList是Java集合框架的动态数组实现，用于存储排除URL列表
import java.util.ArrayList; // 导入ArrayList集合类
// List是Java集合框架的列表接口，定义列表操作规范
import java.util.List; // 导入List接口

/**
 * XSS攻击防护过滤器
 * 对HTTP请求进行XSS（跨站脚本攻击）防护，过滤请求参数和请求体中的恶意脚本
 * 通过包装请求对象为XssHttpServletRequestWrapper实现HTML标签清理
 * 防止用户输入恶意脚本代码，保护系统安全
 *
 * @author ruoyi
 */
// XssFilter实现Filter接口，符合Servlet规范
// 过滤器在请求到达Servlet之前拦截，对请求进行预处理
public class XssFilter implements Filter {
    /**
     * 排除URL列表
     * 存储不需要进行XSS过滤的URL路径模式，从XssProperties配置中加载
     * 使用ArrayList存储，支持动态添加和通配符匹配
     * public修饰符：允许其他类访问此列表（如配置类）
     */
    public List<String> excludes = new ArrayList<>();

    /**
     * 过滤器初始化方法
     * 在过滤器创建时调用，从Spring容器中获取XssProperties配置，加载排除URL列表
     * 使用SpringUtils.getBean()获取配置Bean，实现依赖查找
     *
     * @param filterConfig 过滤器配置对象，包含过滤器的初始化参数
     * @throws ServletException Servlet异常，初始化失败时抛出
     */
    // @Override注解：重写Filter接口的init方法
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 从Spring容器获取XssProperties配置Bean
        // SpringUtils.getBean()通过ApplicationContext获取Bean实例
        XssProperties properties = SpringUtils.getBean(XssProperties.class);
        // 将配置中的排除URL添加到excludes列表
        // addAll方法批量添加，避免循环添加
        excludes.addAll(properties.getExcludeUrls());
    }

    /**
     * 过滤器核心处理方法
     * 在每次请求时调用，判断请求是否需要XSS过滤，如果需要则包装请求对象
     * 实现XSS防护的核心逻辑，对请求参数和请求体进行HTML标签清理
     *
     * @param request  Servlet请求对象，需要转换为HttpServletRequest
     * @param response Servlet响应对象，需要转换为HttpServletResponse
     * @param chain    过滤器链，用于继续执行后续过滤器或Servlet
     * @throws IOException      IO异常，读取请求体或写入响应时可能抛出
     * @throws ServletException Servlet异常，处理请求时可能抛出
     */
    // @Override注解：重写Filter接口的doFilter方法
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        // 将请求转换为HTTP请求对象，ServletRequest是通用接口，HttpServletRequest是HTTP专用接口
        HttpServletRequest req = (HttpServletRequest) request;
        // 将响应转换为HTTP响应对象，ServletResponse是通用接口，HttpServletResponse是HTTP专用接口
        HttpServletResponse resp = (HttpServletResponse) response;
        // 判断是否需要排除XSS过滤，调用handleExcludeURL方法进行判断
        if (handleExcludeURL(req, resp)) {
            // 不需要过滤，直接放行，调用chain.doFilter继续执行过滤器链
            chain.doFilter(request, response);
            // return语句确保后续代码不执行
            return;
        }
        // 创建XSS请求包装器，对请求参数和请求体进行HTML标签清理
        // XssHttpServletRequestWrapper继承HttpServletRequestWrapper，重写获取参数的方法
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        // 使用包装后的请求对象继续过滤器链
        // 后续所有组件获取参数时都会经过XssHttpServletRequestWrapper的清理逻辑
        chain.doFilter(xssRequest, response);
    }

    /**
     * 判断URL是否需要排除XSS过滤
     * 根据请求方法和URL路径判断是否跳过XSS过滤
     * 实现排除逻辑：GET/DELETE请求不过滤，配置中的排除URL不过滤
     *
     * @param request  HTTP请求对象，用于获取请求方法和URL路径
     * @param response HTTP响应对象（当前未使用，保留参数以便扩展）
     * @return true-需要排除（不过滤），false-需要过滤
     */
    // private修饰符：仅在类内部使用，不暴露给外部
    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求的Servlet路径（不包含上下文路径）
        // getServletPath()返回相对于应用上下文的路径，如/api/user
        String url = request.getServletPath();
        // 获取请求方法（GET、POST等）
        // getMethod()返回HTTP方法名，如GET、POST、PUT、DELETE
        String method = request.getMethod();
        // GET和DELETE请求不过滤（通常不包含敏感数据，且GET参数在URL中，过滤可能影响正常业务）
        // method == null判断防止空指针异常
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            // 返回true，表示排除过滤
            return true;
        }
        // 使用通配符匹配判断URL是否在排除列表中
        // StringUtils.matches支持通配符*和?，如/api/*匹配所有/api/开头的URL
        return StringUtils.matches(url, excludes);
    }

    /**
     * 过滤器销毁方法
     * 在过滤器被移除时调用，可用于资源清理
     * 当前实现为空，因为XssFilter没有需要清理的资源
     */
    // @Override注解：重写Filter接口的destroy方法
    @Override
    public void destroy() {
        // 当前实现为空，不需要清理资源
        // 如果过滤器使用了数据库连接、线程池等资源，可以在此方法中释放
    }
}

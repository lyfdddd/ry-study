// Servlet工具类，提供HTTP请求响应处理相关工具方法
// 继承Hutool的JakartaServletUtil，提供增强的Servlet操作能力
package org.dromara.common.core.utils;

// Hutool类型转换工具类（用于参数类型转换）
// Convert是Hutool封装的类型转换工具类，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool Jakarta Servlet工具类（提供Servlet操作基础方法）
// JakartaServletUtil是Hutool封装的Servlet工具类
import cn.hutool.extra.servlet.JakartaServletUtil;
// Hutool HTTP状态码常量（如HTTP_OK = 200）
// HttpStatus提供HTTP状态码常量
import cn.hutool.http.HttpStatus;
// Jakarta Servlet API（Servlet 5.0+）
import jakarta.servlet.ServletRequest; // 通用请求接口，所有请求的根接口
import jakarta.servlet.http.HttpServletRequest; // HTTP请求接口，继承自ServletRequest
import jakarta.servlet.http.HttpServletResponse; // HTTP响应接口
import jakarta.servlet.http.HttpSession; // HTTP会话接口，用于跨请求存储数据
// Lombok注解：设置构造方法访问级别为私有，防止类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;
// Spring HTTP媒体类型（如APPLICATION_JSON_VALUE = "application/json"）
// MediaType提供HTTP媒体类型常量
import org.springframework.http.MediaType;
// Spring不区分大小写的LinkedMap（用于存储请求头）
// LinkedCaseInsensitiveMap是Spring提供的键不区分大小写的Map实现
import org.springframework.util.LinkedCaseInsensitiveMap;
// Spring请求属性（用于获取当前请求）
// RequestAttributes是Spring的请求属性接口
import org.springframework.web.context.request.RequestAttributes;
// Spring请求上下文持有者（存储当前请求线程变量）
// RequestContextHolder是Spring提供的请求上下文持有者，使用ThreadLocal存储
import org.springframework.web.context.request.RequestContextHolder;
// Spring Servlet请求属性（包含请求和响应对象）
// ServletRequestAttributes是RequestAttributes的实现，包含HttpServletRequest和HttpServletResponse
import org.springframework.web.context.request.ServletRequestAttributes;

// Java IO异常
import java.io.IOException;
// Java URL解码工具
import java.net.URLDecoder;
// Java URL编码工具
import java.net.URLEncoder;
// Java字符集（UTF-8）
import java.nio.charset.StandardCharsets;
// Java集合工具（不可变集合）
import java.util.Collections;
// Java枚举接口（用于遍历请求头）
import java.util.Enumeration;
// Java哈希Map
import java.util.HashMap;
// Java Map接口
import java.util.Map;

/**
 * Servlet工具类
 * 继承Hutool的JakartaServletUtil，提供HTTP请求响应处理的常用操作
 * 包括参数获取、请求头处理、Session操作、Ajax判断、IP获取等功能
 * 是RuoYi-Vue-Plus中处理HTTP请求响应的核心工具类
 *
 * @author ruoyi
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// access = AccessLevel.PRIVATE确保构造方法私有，无法从外部创建对象
// 符合工具类的设计模式，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// Servlet工具类，继承Hutool的JakartaServletUtil
public class ServletUtils extends JakartaServletUtil {

    /**
     * 获取指定名称的String类型请求参数
     * 从当前请求的HttpServletRequest中获取参数值
     * 常用于获取表单提交、URL参数等数据
     *
     * @param name 参数名
     * @return 参数值（如果参数不存在返回null）
     */
    // 静态方法，方便全局调用，无需创建对象
    // 获取指定名称的String类型请求参数
    public static String getParameter(String name) {
        // 调用getRequest()获取当前HTTP请求对象，再调用getParameter()获取参数
        // getRequest()使用Spring的RequestContextHolder获取当前线程绑定的请求
        // 这是Spring MVC中获取当前请求的标准方式
        // 调用getRequest()获取当前HTTP请求对象，再调用getParameter()获取参数
        return getRequest().getParameter(name);
    }

    /**
     * 获取指定名称的String类型请求参数，若参数不存在则返回默认值
     * 增强版本，避免null值，提升代码健壮性
     * 适用于需要确保非null返回值的场景
     *
     * @param name         参数名
     * @param defaultValue 默认值（当参数为null或空字符串时返回）
     * @return 参数值或默认值
     */
    // 静态方法，方便全局调用
    // 获取指定名称的String类型请求参数，若参数不存在则返回默认值
    public static String getParameter(String name, String defaultValue) {
        // 使用Hutool的Convert.toStr进行类型转换，如果参数为null则返回defaultValue
        // Convert.toStr是Hutool提供的类型转换方法，支持各种类型转换为String
        // 第二个参数是默认值，当转换失败或值为null时返回
        // 使用Hutool的Convert.toStr进行类型转换，如果参数为null则返回defaultValue
        return Convert.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的Integer类型请求参数
     * 自动将字符串参数转换为Integer类型
     * 常用于获取数字类型的参数，如页码、ID等
     *
     * @param name 参数名
     * @return 参数值（如果参数不存在或格式错误返回null）
     */
    // 静态方法，方便全局调用
    // 获取指定名称的Integer类型请求参数
    public static Integer getParameterToInt(String name) {
        // 使用Hutool的Convert.toInt进行类型转换，转换失败返回null
        // Convert.toInt是Hutool提供的类型转换方法，将String转换为Integer
        // 如果字符串格式不正确或值为null，返回null
        // 使用Hutool的Convert.toInt进行类型转换，转换失败返回null
        return Convert.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的Integer类型请求参数，若参数不存在则返回默认值
     * 增强版本，避免null值，提升代码健壮性
     * 适用于需要确保非null返回值的场景
     *
     * @param name         参数名
     * @param defaultValue 默认值（当参数为null或格式错误时返回）
     * @return 参数值或默认值
     */
    // 静态方法，方便全局调用
    // 获取指定名称的Integer类型请求参数，若参数不存在则返回默认值
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        // 使用Hutool的Convert.toInt进行类型转换，如果参数为null或转换失败返回defaultValue
        // Convert.toInt的第二个参数是默认值，当转换失败时返回
        // 避免调用方进行null值判断
        // 使用Hutool的Convert.toInt进行类型转换，如果参数为null或转换失败返回defaultValue
        return Convert.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的Boolean类型请求参数
     * 自动将字符串参数转换为Boolean类型
     * 支持"true"、"false"、"1"、"0"、"yes"、"no"等格式
     * 常用于获取开关类型的参数
     *
     * @param name 参数名
     * @return 参数值（如果参数不存在返回null）
     */
    // 静态方法，方便全局调用
    // 获取指定名称的Boolean类型请求参数
    public static Boolean getParameterToBool(String name) {
        // 使用Hutool的Convert.toBool进行类型转换
        // Convert.toBool是Hutool提供的类型转换方法，将String转换为Boolean
        // 支持多种布尔值格式，不区分大小写
        // 使用Hutool的Convert.toBool进行类型转换
        return Convert.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的Boolean类型请求参数，若参数不存在则返回默认值
     * 增强版本，避免null值，提升代码健壮性
     * 适用于需要确保非null返回值的场景
     *
     * @param name         参数名
     * @param defaultValue 默认值（当参数为null时返回）
     * @return 参数值或默认值
     */
    // 静态方法，方便全局调用
    // 获取指定名称的Boolean类型请求参数，若参数不存在则返回默认值
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        // 使用Hutool的Convert.toBool进行类型转换，如果参数为null返回defaultValue
        // Convert.toBool的第二个参数是默认值，当转换失败时返回
        // 避免调用方进行null值判断
        // 使用Hutool的Convert.toBool进行类型转换，如果参数为null返回defaultValue
        return Convert.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取所有请求参数（以Map形式返回）
     * 返回不可修改的Map，防止外部修改原始参数
     * 符合防御式编程原则，保护数据不被意外修改
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 请求参数的Map，键为参数名，值为参数值数组（因为一个参数名可能有多个值）
     */
    // 静态方法，方便全局调用
    // 获取所有请求参数（以Map形式返回）
    public static Map<String, String[]> getParams(ServletRequest request) {
        // 调用ServletRequest.getParameterMap()获取所有参数Map
        // getParameterMap()返回Map<String, String[]>，键是参数名，值是参数值数组
        // 因为一个参数名可能对应多个值（如多选框）
        // 调用ServletRequest.getParameterMap()获取所有参数Map
        final Map<String, String[]> map = request.getParameterMap();
        // 返回Collections.unmodifiableMap包装的不可修改Map，防止外部修改
        // 这是防御式编程，保护原始数据不被意外修改
        // 返回不可修改的Map，防止外部修改原始参数
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获取所有请求参数（以Map形式返回，值为字符串形式的拼接）
     * 将参数值数组用逗号拼接为单个字符串，方便使用
     * 适用于大多数参数只有一个值的场景
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 请求参数的Map，键为参数名，值为拼接后的字符串
     */
    // 静态方法，方便全局调用
    // 获取所有请求参数（以Map形式返回，值为字符串形式的拼接）
    public static Map<String, String> getParamMap(ServletRequest request) {
        // 创建新的HashMap用于存储处理后的参数
        Map<String, String> params = new HashMap<>();
        // 遍历getParams返回的参数Map（键值对形式）
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            // 将参数值数组用逗号拼接为单个字符串
            // StringUtils.joinComma是自定义方法，将数组用逗号连接
            // 适用于大多数参数只有一个值的场景
            // 将参数值数组用逗号拼接为单个字符串
            params.put(entry.getKey(), StringUtils.joinComma(entry.getValue()));
        }
        // 返回处理后的参数Map
        // 返回处理后的参数Map
        return params;
    }

    /**
     * 获取当前HTTP请求对象
     * 通过Spring的RequestContextHolder获取当前线程绑定的请求
     * 适用于Controller、Service等层获取当前请求
     * 是Spring MVC中获取当前请求的标准方式
     *
     * @return 当前HTTP请求对象（HttpServletRequest）
     */
    // 静态方法，方便全局调用
    // 获取当前HTTP请求对象
    public static HttpServletRequest getRequest() {
        try {
            // 调用getRequestAttributes()获取ServletRequestAttributes
            // 再调用getRequest()获取HttpServletRequest对象
            // RequestContextHolder使用ThreadLocal存储当前请求，确保线程安全
            // 调用getRequestAttributes()获取ServletRequestAttributes，再调用getRequest()获取HttpServletRequest对象
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            // 如果获取失败（如非Web环境、异步线程等），返回null
            // 避免抛出异常，让调用方自行处理null值
            // 如果获取失败，返回null
            return null;
        }
    }

    /**
     * 获取当前HTTP响应对象
     * 通过Spring的RequestContextHolder获取当前线程绑定的响应
     * 适用于需要直接操作响应的场景（如自定义渲染、文件下载等）
     *
     * @return 当前HTTP响应对象（HttpServletResponse）
     */
    // 静态方法，方便全局调用
    // 获取当前HTTP响应对象
    public static HttpServletResponse getResponse() {
        try {
            // 调用getRequestAttributes()获取ServletRequestAttributes
            // 再调用getResponse()获取HttpServletResponse对象
            // RequestContextHolder使用ThreadLocal存储当前响应，确保线程安全
            // 调用getRequestAttributes()获取ServletRequestAttributes，再调用getResponse()获取HttpServletResponse对象
            return getRequestAttributes().getResponse();
        } catch (Exception e) {
            // 如果获取失败（如非Web环境、异步线程等），返回null
            // 避免抛出异常，让调用方自行处理null值
            // 如果获取失败，返回null
            return null;
        }
    }

    /**
     * 获取当前请求的HttpSession对象
     * <p>
     * 如果当前请求已经关联了一个会话（即已经存在有效的session ID），
     * 则返回该会话对象；如果没有关联会话，则会创建一个新的会话对象并返回。
     * <p>
     * HttpSession用于存储会话级别的数据，如用户登录信息、购物车内容等，
     * 可以在多个请求之间共享会话数据
     * 是Servlet规范提供的会话管理机制
     *
     * @return 当前请求的HttpSession对象
     */
    // 静态方法，方便全局调用
    // 获取当前请求的HttpSession对象
    public static HttpSession getSession() {
        // 调用getRequest()获取当前HTTP请求对象，再调用getSession()获取会话
        // getSession()无参方法：如果会话不存在则创建新会话（等同于getSession(true)）
        // 这是Servlet规范提供的会话获取方式
        // 调用getRequest()获取当前HTTP请求对象，再调用getSession()获取会话
        return getRequest().getSession();
    }

    /**
     * 获取当前请求的请求属性（ServletRequestAttributes）
     * 通过Spring的RequestContextHolder获取请求属性，包含请求和响应对象
     * 是Spring MVC中获取请求属性的标准方式
     *
     * @return {@link ServletRequestAttributes}请求属性对象
     */
    // 静态方法，方便全局调用
    // 获取当前请求的请求属性（ServletRequestAttributes）
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            // 从RequestContextHolder获取当前线程绑定的RequestAttributes
            // Spring框架在每个请求开始时将请求属性绑定到当前线程
            // RequestContextHolder使用ThreadLocal实现，确保线程安全
            // 从RequestContextHolder获取当前线程绑定的RequestAttributes
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            // 强制转换为ServletRequestAttributes（包含HttpServletRequest和HttpServletResponse）
            // ServletRequestAttributes是RequestAttributes的实现类
            // 强制转换为ServletRequestAttributes
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            // 如果获取失败（如非Web环境、异步线程等），返回null
            // 避免抛出异常，让调用方自行处理null值
            // 如果获取失败，返回null
            return null;
        }
    }

    /**
     * 获取指定请求头的值，如果头部为空则返回空字符串
     * 自动进行URL解码，处理编码后的头部值
     * 常用于获取自定义请求头、认证头等
     *
     * @param request 请求对象
     * @param name    头部名称（如"User-Agent"、"Authorization"等）
     * @return 头部值（已URL解码，如果为空返回空字符串）
     */
    // 静态方法，方便全局调用
    // 获取指定请求头的值，如果头部为空则返回空字符串
    public static String getHeader(HttpServletRequest request, String name) {
        // 调用HttpServletRequest.getHeader()获取指定名称的请求头
        // getHeader()返回指定名称的请求头值，如果不存在返回null
        // 调用HttpServletRequest.getHeader()获取指定名称的请求头
        String value = request.getHeader(name);
        // 如果值为null或空字符串，返回空字符串常量
        // 避免返回null，让调用方无需进行null值判断
        // 如果值为null或空字符串，返回空字符串常量
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        // 调用urlDecode()进行URL解码（处理可能存在的URL编码）
        // 某些场景下请求头值可能被URL编码，需要解码
        // 调用urlDecode()进行URL解码
        return urlDecode(value);
    }

    /**
     * 获取所有请求头的Map，键为头部名称，值为头部值
     * 使用LinkedCaseInsensitiveMap实现，键不区分大小写
     * 符合HTTP协议规范（HTTP头部字段名不区分大小写）
     *
     * @param request 请求对象
     * @return 请求头的Map（键不区分大小写）
     */
    // 静态方法，方便全局调用
    // 获取所有请求头的Map，键为头部名称，值为头部值
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        // 创建LinkedCaseInsensitiveMap，键不区分大小写
        // 符合HTTP规范：Header名称不区分大小写（如Content-Type和content-type是同一个头）
        // LinkedCaseInsensitiveMap是Spring提供的实现，性能优异
        // 创建LinkedCaseInsensitiveMap，键不区分大小写
        Map<String, String> map = new LinkedCaseInsensitiveMap<>();
        // 获取所有请求头名称的枚举
        // getHeaderNames()返回所有请求头名称的Enumeration
        // 获取所有请求头名称的枚举
        Enumeration<String> enumeration = request.getHeaderNames();
        // 如果枚举不为null，遍历所有请求头
        // 如果枚举不为null，遍历所有请求头
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                // 获取头部名称
                // 获取头部名称
                String key = enumeration.nextElement();
                // 获取头部值
                // 获取头部值
                String value = request.getHeader(key);
                // 放入Map中
                // 放入Map中
                map.put(key, value);
            }
        }
        // 返回包含所有请求头的Map
        // 返回包含所有请求头的Map
        return map;
    }

    /**
     * 将字符串渲染到客户端（以JSON格式返回）
     * 设置响应状态码200、Content-Type为application/json、UTF-8编码
     * 常用于自定义响应、异常处理、过滤器等场景
     * 是构建RESTful API的基础方法
     *
     * @param response 响应对象
     * @param string   待渲染的JSON字符串
     */
    // 静态方法，方便全局调用
    // 将字符串渲染到客户端（以JSON格式返回）
    public static void renderString(HttpServletResponse response, String string) {
        try {
            // 设置HTTP状态码为200（OK）
            // HTTP_OK是Hutool提供的常量，值为200
            // 设置HTTP状态码为200（OK）
            response.setStatus(HttpStatus.HTTP_OK);
            // 设置Content-Type为application/json，告诉客户端返回JSON数据
            // APPLICATION_JSON_VALUE是Spring提供的常量，值为"application/json"
            // 设置Content-Type为application/json
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            // 设置字符编码为UTF-8，支持中文
            // UTF_8是Java标准库提供的字符集常量
            // 设置字符编码为UTF-8
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            // 调用response.getWriter().print()将字符串写入响应体
            // getWriter()获取PrintWriter对象，用于向客户端输出文本
            // 调用response.getWriter().print()将字符串写入响应体
            response.getWriter().print(string);
        } catch (IOException e) {
            // 捕获IO异常并打印堆栈（如客户端断开连接、网络异常等）
            // 记录异常信息，方便排查问题
            // 捕获IO异常并打印堆栈
            e.printStackTrace();
        }
    }

    /**
     * 判断当前请求是否为Ajax异步请求
     * 通过多种方式综合判断，提高准确性
     * 支持多种前端框架（jQuery、Axios、Vue、React等）
     *
     * @param request 请求对象
     * @return true表示是Ajax请求，false表示是普通页面请求
     */
    // 静态方法，方便全局调用
    // 判断当前请求是否为Ajax异步请求
    public static boolean isAjaxRequest(HttpServletRequest request) {

        // 方式1：判断Accept头部是否包含application/json
        // Ajax请求通常设置Accept: application/json, text/javascript, */*
        // Accept头部告诉服务器客户端期望的响应格式
        // 方式1：判断Accept头部是否包含application/json
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            // 如果Accept包含application/json，认为是Ajax请求
            // 如果Accept包含application/json，认为是Ajax请求
            return true;
        }

        // 方式2：判断X-Requested-With头部是否包含XMLHttpRequest
        // 大多数前端框架（jQuery、Axios等）会自动设置X-Requested-With: XMLHttpRequest
        // 这是Prototype.js引入的约定，被广泛使用
        // 方式2：判断X-Requested-With头部是否包含XMLHttpRequest
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            // 如果X-Requested-With包含XMLHttpRequest，认为是Ajax请求
            // 如果X-Requested-With包含XMLHttpRequest，认为是Ajax请求
            return true;
        }

        // 方式3：判断URI后缀是否为.json或.xml
        // RESTful风格的API可能使用扩展名区分响应格式
        // 例如：/user/1.json表示获取JSON格式的用户数据
        // 方式3：判断URI后缀是否为.json或.xml
        String uri = request.getRequestURI();
        if (StringUtils.equalsAnyIgnoreCase(uri, ".json", ".xml")) {
            // 如果URI以.json或.xml结尾，认为是Ajax请求
            // 如果URI以.json或.xml结尾，认为是Ajax请求
            return true;
        }

        // 方式4：判断请求参数__ajax是否为json或xml
        // 某些场景通过参数指定响应格式
        // 例如：/user/1?__ajax=json
        // 方式4：判断请求参数__ajax是否为json或xml
        String ajax = request.getParameter("__ajax");
        // 如果__ajax参数值为json或xml，认为是Ajax请求
        // 如果__ajax参数值为json或xml，认为是Ajax请求
        return StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 获取客户端IP地址
     * 封装Hutool的getClientIP方法，自动获取当前请求的客户端IP
     * 会尝试从X-Forwarded-For、X-Real-IP等代理头中获取真实IP
     * 支持反向代理场景，如Nginx、Apache等
     *
     * @return 客户端IP地址（如"192.168.1.100"）
     */
    // 静态方法，方便全局调用
    // 获取客户端IP地址
    public static String getClientIP() {
        // 调用getClientIP(HttpServletRequest)方法，传入当前请求对象
        // 这是Hutool提供的获取客户端IP的方法，支持代理场景
        // 调用getClientIP(HttpServletRequest)方法，传入当前请求对象
        return getClientIP(getRequest());
    }

    /**
     * 对内容进行URL编码
     * 使用UTF-8字符集进行URL编码，将特殊字符转换为%xx格式
     * 常用于URL参数拼接、重定向等场景
     * 例如：urlEncode("中文") -> "%E4%B8%AD%E6%96%87"
     *
     * @param str 原始字符串
     * @return URL编码后的字符串
     */
    // 静态方法，方便全局调用
    // 对内容进行URL编码
    public static String urlEncode(String str) {
        // 调用URLEncoder.encode进行URL编码，指定UTF-8字符集
        // URLEncoder是Java标准库提供的URL编码工具类
        // StandardCharsets.UTF_8指定使用UTF-8字符集，避免中文乱码
        // 调用URLEncoder.encode进行URL编码，指定UTF-8字符集
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * 对内容进行URL解码
     * 使用UTF-8字符集进行URL解码，将%xx格式转换回原始字符
     * 常用于解析URL参数、处理编码后的请求头等
     * 例如：urlDecode("%E4%B8%AD%E6%96%87") -> "中文"
     *
     * @param str URL编码后的字符串
     * @return 解码后的原始字符串
     */
    // 静态方法，方便全局调用
    // 对内容进行URL解码
    public static String urlDecode(String str) {
        // 调用URLDecoder.decode进行URL解码，指定UTF-8字符集
        // URLDecoder是Java标准库提供的URL解码工具类
        // StandardCharsets.UTF_8指定使用UTF-8字符集，避免中文乱码
        // 调用URLDecoder.decode进行URL解码，指定UTF-8字符集
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

}

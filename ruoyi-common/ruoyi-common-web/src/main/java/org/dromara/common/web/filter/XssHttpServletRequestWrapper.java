package org.dromara.common.web.filter;

// 导入Hutool的IO工具类，提供流操作便捷方法
import cn.hutool.core.io.IoUtil;
// 导入Hutool的Map工具类，用于Map判空操作
import cn.hutool.core.map.MapUtil;
// 导入Hutool的数组工具类，用于数组判空操作
import cn.hutool.core.util.ArrayUtil;
// 导入Hutool的字符串工具类，用于字符串转换
import cn.hutool.core.util.StrUtil;
// 导入Hutool的HTML工具类，提供HTML标签清理功能，防止XSS攻击
import cn.hutool.http.HtmlUtil;
// 导入Servlet读取监听器接口
import jakarta.servlet.ReadListener;
// 导入Servlet输入流
import jakarta.servlet.ServletInputStream;
// 导入HTTP请求接口
import jakarta.servlet.http.HttpServletRequest;
// 导入HTTP请求包装器基类
import jakarta.servlet.http.HttpServletRequestWrapper;
// 导入字符串工具类，用于字符串判断
import org.dromara.common.core.utils.StringUtils;
// 导入Spring的HTTP头常量
import org.springframework.http.HttpHeaders;
// 导入Spring的MediaType常量，用于判断请求内容类型
import org.springframework.http.MediaType;

// 导入Java IO相关类
import java.io.ByteArrayInputStream; // 导入字节数组输入流
import java.io.IOException; // 导入IO异常类
import java.nio.charset.StandardCharsets; // 导入标准字符集常量
import java.util.HashMap; // 导入HashMap集合类
import java.util.Map; // 导入Map接口

/**
 * XSS攻击防护请求包装器
 * 继承HttpServletRequestWrapper，对请求参数和请求体进行HTML标签清理
 * 防止XSS（跨站脚本攻击）注入恶意脚本
 *
 * @author ruoyi
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 构造函数
     * 调用父类构造函数保存原始请求对象
     *
     * @param request HTTP请求对象
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        // 调用父类构造函数，保存原始请求对象
        super(request);
    }

    /**
     * 获取单个请求参数值
     * 对参数值进行HTML标签清理和前后空格过滤
     *
     * @param name 参数名
     * @return 清理后的参数值
     */
    @Override
    public String getParameter(String name) {
        // 从父类获取原始参数值
        String value = super.getParameter(name);
        // 如果值为null，直接返回null
        if (value == null) {
            return null;
        }
        // 使用Hutool的HtmlUtil清理HTML标签，并去除前后空格
        // 防止XSS攻击，移除所有HTML标签
        return HtmlUtil.cleanHtmlTag(value).trim();
    }

    /**
     * 获取所有请求参数Map
     * 对Map中所有参数值进行HTML标签清理和前后空格过滤
     *
     * @return 清理后的参数Map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        // 从父类获取原始参数Map
        Map<String, String[]> valueMap = super.getParameterMap();
        // 如果Map为空，直接返回
        if (MapUtil.isEmpty(valueMap)) {
            return valueMap;
        }
        // 为了避免某些容器不允许修改原始参数Map，创建一份副本进行修改
        // 创建新的HashMap，容量为原始Map大小
        Map<String, String[]> map = new HashMap<>(valueMap.size());
        // 将原始Map所有数据复制到新Map
        map.putAll(valueMap);
        // 遍历Map中的所有键值对
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            // 获取参数值数组
            String[] values = entry.getValue();
            // 如果值不为null
            if (values != null) {
                // 获取数组长度
                int length = values.length;
                // 创建新的字符串数组存储清理后的值
                String[] escapseValues = new String[length];
                // 遍历数组每个元素
                for (int i = 0; i < length; i++) {
                    // 对每个值进行HTML标签清理和前后空格过滤
                    // 防止XSS攻击，移除所有HTML标签
                    escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
                }
                // 将清理后的数组放回Map
                map.put(entry.getKey(), escapseValues);
            }
        }
        // 返回清理后的Map
        return map;
    }

    /**
     * 获取指定参数名的所有值
     * 对参数值数组进行HTML标签清理和前后空格过滤
     *
     * @param name 参数名
     * @return 清理后的参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        // 从父类获取原始参数值数组
        String[] values = super.getParameterValues(name);
        // 如果数组为空，直接返回
        if (ArrayUtil.isEmpty(values)) {
            return values;
        }
        // 获取数组长度
        int length = values.length;
        // 创建新的字符串数组存储清理后的值
        String[] escapseValues = new String[length];
        // 遍历数组每个元素
        for (int i = 0; i < length; i++) {
            // 对每个值进行HTML标签清理和前后空格过滤
            // 防止XSS攻击，移除所有HTML标签
            escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
        }
        // 返回清理后的数组
        return escapseValues;
    }

    /**
     * 获取Servlet输入流
     * 对JSON请求体进行HTML标签清理和前后空格过滤
     *
     * @return ServletInputStream对象
     * @throws IOException IO异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 如果不是JSON请求类型，直接返回原始输入流
        if (!isJsonRequest()) {
            return super.getInputStream();
        }

        // 读取请求体内容为字符串
        // 使用Hutool的IoUtil读取字节数组，再转换为UTF-8字符串
        String json = StrUtil.str(IoUtil.readBytes(super.getInputStream(), false), StandardCharsets.UTF_8);
        // 如果请求体为空，直接返回原始输入流
        if (StringUtils.isEmpty(json)) {
            return super.getInputStream();
        }

        // 对JSON字符串进行XSS过滤
        // 使用Hutool的HtmlUtil清理HTML标签，并去除前后空格
        json = HtmlUtil.cleanHtmlTag(json).trim();
        // 将清理后的字符串转换为UTF-8字节数组
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        // 从字节数组创建ByteArrayInputStream
        final ByteArrayInputStream bis = IoUtil.toStream(jsonBytes);
        // 返回自定义的ServletInputStream实现
        return new ServletInputStream() {
            /**
             * 是否读取完成
             * 对于ByteArrayInputStream，始终返回true（表示可以重复读取）
             *
             * @return true
             */
            @Override
            public boolean isFinished() {
                return true;
            }

            /**
             * 是否准备就绪
             * 对于ByteArrayInputStream，始终返回true
             *
             * @return true
             */
            @Override
            public boolean isReady() {
                return true;
            }

            /**
             * 获取可读字节数
             * 返回字节数组的长度
             *
             * @return 可读字节数
             * @throws IOException IO异常
             */
            @Override
            public int available() throws IOException {
                return jsonBytes.length;
            }

            /**
             * 设置读取监听器
             * 当前实现为空，不支持异步读取监听
             *
             * @param readListener 读取监听器
             */
            @Override
            public void setReadListener(ReadListener readListener) {
                // 空实现，不支持异步读取监听
            }

            /**
             * 读取一个字节
             * 委托给ByteArrayInputStream的read方法
             *
             * @return 读取的字节，-1表示流结束
             * @throws IOException IO异常
             */
            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };
    }

    /**
     * 判断是否为JSON请求
     * 检查Content-Type请求头是否以application/json开头
     *
     * @return true-是JSON请求，false-不是JSON请求
     */
    public boolean isJsonRequest() {
        // 从请求头获取Content-Type
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        // 使用StringUtils判断Content-Type是否以application/json开头（忽略大小写）
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }
}

package org.dromara.common.web.filter;

// 导入Hutool的IO工具类，提供流操作便捷方法
import cn.hutool.core.io.IoUtil;
// 导入系统常量类，获取UTF-8编码常量
import org.dromara.common.core.constant.Constants;

// 导入Servlet API相关类
import jakarta.servlet.ReadListener; // 导入读取监听器接口
import jakarta.servlet.ServletInputStream; // 导入Servlet输入流
import jakarta.servlet.ServletResponse; // 导入Servlet响应接口
import jakarta.servlet.http.HttpServletRequest; // 导入HTTP请求接口
import jakarta.servlet.http.HttpServletRequestWrapper; // 导入HTTP请求包装器基类

// 导入Java IO相关类
import java.io.BufferedReader; // 导入缓冲字符输入流
import java.io.ByteArrayInputStream; // 导入字节数组输入流
import java.io.IOException; // 导入IO异常类
import java.io.InputStreamReader; // 导入输入流读取器

/**
 * 可重复读取请求体的HTTP请求包装器
 * 通过缓存请求体内容到字节数组，实现InputStream的多次读取
 * 解决HTTP请求流只能读取一次的问题，支持AOP日志记录和业务处理同时读取请求体
 *
 * @author ruoyi
 */
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 请求体内容缓存字节数组
     * 在构造函数中读取并缓存，后续所有读取操作都从这个数组获取
     */
    private final byte[] body;

    /**
     * 构造函数
     * 读取并缓存请求体内容，设置请求和响应的字符编码为UTF-8
     *
     * @param request  HTTP请求对象
     * @param response Servlet响应对象
     * @throws IOException IO异常
     */
    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException {
        // 调用父类构造函数，保存原始请求对象
        super(request);
        // 设置请求的字符编码为UTF-8，确保正确解析中文
        request.setCharacterEncoding(Constants.UTF8);
        // 设置响应的字符编码为UTF-8，确保返回内容编码正确
        response.setCharacterEncoding(Constants.UTF8);

        // 读取请求体内容并缓存到字节数组
        // 第二个参数false表示不关闭原始输入流
        body = IoUtil.readBytes(request.getInputStream(), false);
    }

    /**
     * 获取字符输入流
     * 从缓存的字节数组创建BufferedReader，支持多次读取
     *
     * @return BufferedReader对象
     * @throws IOException IO异常
     */
    @Override
    public BufferedReader getReader() throws IOException {
        // 从getInputStream()创建InputStreamReader，再包装为BufferedReader
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * 获取Servlet输入流
     * 从缓存的字节数组创建ByteArrayInputStream，实现多次读取
     *
     * @return ServletInputStream对象
     * @throws IOException IO异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 从缓存的字节数组创建ByteArrayInputStream
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        // 返回自定义的ServletInputStream实现
        return new ServletInputStream() {
            /**
             * 读取一个字节
             * 委托给ByteArrayInputStream的read方法
             *
             * @return 读取的字节，-1表示流结束
             * @throws IOException IO异常
             */
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            /**
             * 获取可读字节数
             * 返回缓存数组的长度
             *
             * @return 可读字节数
             * @throws IOException IO异常
             */
            @Override
            public int available() throws IOException {
                return body.length;
            }

            /**
             * 是否读取完成
             * 对于ByteArrayInputStream，始终返回false（表示可以重复读取）
             *
             * @return false
             */
            @Override
            public boolean isFinished() {
                return false;
            }

            /**
             * 是否准备就绪
             * 对于ByteArrayInputStream，始终返回false
             *
             * @return false
             */
            @Override
            public boolean isReady() {
                return false;
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
        };
    }
}

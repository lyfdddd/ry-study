// 文件处理工具类，提供文件下载相关的HTTP响应头设置功能
package org.dromara.common.core.utils.file;

import cn.hutool.core.io.FileUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件处理工具类
 * 基于Hutool的FileUtil进行扩展，提供文件下载相关的HTTP响应头设置功能
 * 主要用于处理文件下载时的文件名编码问题，解决中文文件名乱码
 * 继承自Hutool的FileUtil，拥有Hutool的所有文件操作能力
 * 提供统一的文件下载响应头设置，确保浏览器兼容性
 *
 * @author Lion Li
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils extends FileUtil {

    /**
     * 设置文件下载的HTTP响应头
     * 处理文件名编码问题，支持中文文件名，解决不同浏览器的兼容性
     * 设置Content-Disposition头，告诉浏览器以附件形式下载文件
     * 同时设置CORS相关头，支持跨域文件下载
     * 使用RFC 5987标准的filename*参数，支持UTF-8编码的文件名
     *
     * @param response HTTP响应对象，用于设置响应头
     * @param realFileName 真实文件名，可能包含中文等特殊字符
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) {
        // 对文件名进行百分号编码，处理特殊字符
        // 百分号编码可以确保文件名中的特殊字符在HTTP头中正确传输
        String percentEncodedFileName = percentEncode(realFileName);
        // 构建Content-Disposition响应头值
        // 格式：attachment; filename=编码后的文件名;filename*=utf-8''编码后的文件名
        // filename*是RFC 5987标准，支持UTF-8编码的文件名
        // 使用formatted方法进行字符串格式化，比String.format更简洁
        String contentDispositionValue = "attachment; filename=%s;filename*=utf-8''%s".formatted(percentEncodedFileName, percentEncodedFileName);
        // 设置CORS暴露的响应头，让前端可以访问这些头信息
        // 默认情况下，跨域请求只能访问有限的响应头，通过Access-Control-Expose-Headers可以暴露自定义头
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        // 设置Content-Disposition头，告诉浏览器以附件形式下载
        // Content-disposition告诉浏览器这是一个附件，应该下载而不是直接打开
        response.setHeader("Content-disposition", contentDispositionValue);
        // 设置download-filename头，用于前端获取文件名
        // 前端可以通过这个头获取原始文件名，用于显示下载进度或保存对话框
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 百分号编码工具方法
     * 对字符串进行URL编码，并将空格编码为%20（而不是+号）
     * 符合RFC 3986标准，用于文件名编码
     * URLEncoder默认将空格编码为+号，但在文件名中应该使用%20
     * 这个方法解决了URLEncoder的默认行为与文件名编码需求不一致的问题
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串，空格被编码为%20
     */
    public static String percentEncode(String s) {
        // 使用URLEncoder进行URL编码，指定UTF-8字符集
        // UTF-8是Web标准编码，支持所有Unicode字符
        // URLEncoder.encode方法会抛出异常，但StandardCharsets.UTF_8不会抛出UnsupportedEncodingException
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
        // 将+号替换为%20，因为URLEncoder会将空格编码为+号，但在文件名中应该使用%20
        // 这是URL编码标准的一个特殊情况：在查询字符串中空格编码为+号，在路径中空格编码为%20
        // 文件名属于路径部分，所以应该使用%20
        return encode.replaceAll("\\+", "%20");
    }
}

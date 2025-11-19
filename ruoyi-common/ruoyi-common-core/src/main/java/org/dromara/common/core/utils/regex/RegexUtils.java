package org.dromara.common.core.utils.regex;

import cn.hutool.core.util.ReUtil;
import org.dromara.common.core.constant.RegexConstants;

/**
 * 正则表达式工具类
 * 基于Hutool的ReUtil工具类进行扩展，提供正则表达式相关的增强功能
 * 主要用于字符串匹配、提取等操作
 *
 * @author Feng
 */
// 使用final修饰符，防止类被继承
public final class RegexUtils extends ReUtil {

    /**
     * 从输入字符串中提取匹配正则表达式的部分
     * 如果匹配成功则返回第一个匹配组，如果匹配失败则返回指定的默认值
     * 提供异常保护，防止正则表达式错误导致程序崩溃
     *
     * @param input        要提取的输入字符串
     * @param regex        用于匹配的正则表达式，可以使用 {@link RegexConstants} 中定义的常量
     * @param defaultInput 如果没有匹配时返回的默认值
     * @return 如果找到匹配的部分，则返回匹配的部分，否则返回默认值
     */
    public static String extractFromString(String input, String regex, String defaultInput) {
        try {
            // 使用Hutool的ReUtil.get方法提取第一个匹配组
            // 参数1：正则表达式，参数2：输入字符串，参数3：匹配组索引（1表示第一个括号组）
            String str = ReUtil.get(regex, input, 1);
            // 如果匹配结果为null，返回默认值；否则返回匹配结果
            return str == null ? defaultInput : str;
        } catch (Exception e) {
            // 捕获正则表达式匹配过程中的异常，防止程序崩溃
            // 出现异常时返回默认值，保证方法的健壮性
            return defaultInput;
        }
    }

}

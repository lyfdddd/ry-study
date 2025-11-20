package org.dromara.common.web.core;

import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * 获取请求头国际化信息
 *
 * @author Lion Li
 */
// 自定义LocaleResolver实现类，用于解析请求中的国际化信息
public class I18nLocaleResolver implements LocaleResolver {

    // 解析请求中的Locale信息
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        // 从请求头中获取content-language，格式如：zh_CN、en_US
        String language = httpServletRequest.getHeader("content-language");
        // 默认使用系统默认的Locale
        Locale locale = Locale.getDefault();
        // 如果请求头中包含语言信息
        if (language != null && language.length() > 0) {
            // 按下划线分割，第一部分是语言代码，第二部分是国家代码
            String[] split = language.split("_");
            // 创建Locale对象，如new Locale("zh", "CN")
            locale = new Locale(split[0], split[1]);
        }
        // 返回解析到的Locale
        return locale;
    }

    // 设置Locale的方法，当前实现为空，不支持动态修改
    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
        // 空实现，不处理Locale设置
    }
}

package org.dromara.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化消息工具类
 * 封装Spring的MessageSource，提供统一的国际化消息获取入口
 * 支持多语言环境下的消息翻译，根据当前线程的Locale自动选择语言
 *
 * @author Lion Li
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {

    /**
     * Spring国际化消息源
     * 从Spring容器中获取MessageSource单例，用于读取国际化资源文件
     * 支持.properties文件中的多语言消息定义
     */
    private static final MessageSource MESSAGE_SOURCE = SpringUtils.getBean(MessageSource.class);

    /**
     * 根据消息键和参数获取国际化消息
     * 委托给Spring的MessageSource进行消息翻译
     * 使用当前线程的Locale自动选择语言版本
     *
     * @param code 消息键（对应国际化资源文件中的key）
     * @param args 消息参数（用于替换消息中的占位符，如{0}、{1}等）
     * @return 国际化翻译值，如果找不到对应消息则返回code本身
     */
    public static String message(String code, Object... args) {
        try {
            // 调用Spring的MessageSource获取国际化消息
            // LocaleContextHolder.getLocale()获取当前线程的Locale（语言环境）
            // 根据用户的语言偏好自动选择对应的消息文件（如messages_zh_CN.properties）
            return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            // 如果找不到对应的消息（消息键不存在），返回code本身，避免抛出异常
            // 这样可以保证程序的健壮性，即使消息键错误也不会导致程序崩溃
            return code;
        }
    }
}

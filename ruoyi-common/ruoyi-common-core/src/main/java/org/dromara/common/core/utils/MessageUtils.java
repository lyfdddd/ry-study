// 国际化消息工具类包声明，提供统一的国际化消息获取入口
// 封装Spring的MessageSource，简化国际化消息获取流程
// 是RuoYi-Vue-Plus实现多语言支持的核心组件
package org.dromara.common.core.utils;

// Lombok注解：设置构造方法访问级别为私有，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;
// Spring国际化消息源接口，用于读取国际化资源文件
// MessageSource是Spring提供的国际化核心接口，支持多语言消息管理
import org.springframework.context.MessageSource;
// Spring未找到消息异常，当消息键不存在时抛出
// NoSuchMessageException继承自RuntimeException，表示消息查找失败
import org.springframework.context.NoSuchMessageException;
// Spring区域上下文持有者，用于获取当前线程的Locale（语言环境）
// LocaleContextHolder使用ThreadLocal存储当前线程的语言环境，确保线程安全
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化消息工具类
 * 封装Spring的MessageSource，提供统一的国际化消息获取入口
 * 支持多语言环境下的消息翻译，根据当前线程的Locale自动选择语言
 * 是RuoYi-Vue-Plus实现国际化的核心工具类
 *
 * @author Lion Li
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// access = AccessLevel.PRIVATE确保构造方法私有，无法从外部创建对象
// 符合工具类的设计模式，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// 国际化消息工具类，提供统一的国际化消息获取入口
public class MessageUtils {

    /**
     * Spring国际化消息源
     * 从Spring容器中获取MessageSource单例，用于读取国际化资源文件
     * 支持.properties文件中的多语言消息定义
     * 在Spring Boot启动时自动注入，支持messages_zh_CN.properties、messages_en_US.properties等
     * 采用单例模式，确保全局只有一个MessageSource实例，避免重复创建
     */
    // 使用SpringUtils从Spring容器中获取MessageSource Bean
    // 这是单例模式的应用，确保全局只有一个MessageSource实例
    // 从Spring容器中获取MessageSource单例，用于读取国际化资源文件
    private static final MessageSource MESSAGE_SOURCE = SpringUtils.getBean(MessageSource.class);

    /**
     * 根据消息键和参数获取国际化消息
     * 委托给Spring的MessageSource进行消息翻译
     * 使用当前线程的Locale自动选择语言版本
     * 支持消息参数替换，如"Hello {0}" -> "Hello World"
     *
     * @param code 消息键（对应国际化资源文件中的key，如"user.login.success"）
     * @param args 消息参数（用于替换消息中的占位符，如{0}、{1}等）
     * @return 国际化翻译值，如果找不到对应消息则返回code本身
     */
    // 静态方法，方便全局调用，无需创建对象
    // 使用可变参数支持任意数量的消息参数
    // 根据消息键和参数获取国际化消息
    public static String message(String code, Object... args) {
        try {
            // 调用Spring的MessageSource.getMessage()方法获取国际化消息
            // 第一个参数code是消息键，对应资源文件中的key
            // 第二个参数args是消息参数数组，用于替换占位符
            // 第三个参数LocaleContextHolder.getLocale()获取当前线程的Locale（语言环境）
            // 根据用户的语言偏好自动选择对应的消息文件（如messages_zh_CN.properties）
            // 这是Spring国际化机制的核心API
            // 调用Spring的MessageSource.getMessage()方法获取国际化消息
            return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            // 捕获NoSuchMessageException异常，表示消息键不存在
            // 如果找不到对应的消息（消息键不存在或资源文件配置错误），返回code本身
            // 这样可以保证程序的健壮性，即使消息键错误也不会导致程序崩溃
            // 避免因为国际化配置问题影响业务功能
            // 如果找不到对应的消息，返回code本身，保证程序健壮性
            return code;
        }
    }
}

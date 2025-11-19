// 基于Hutool的对象工具类，提供空值安全的属性访问方法
package org.dromara.common.core.utils;

import cn.hutool.core.util.ObjectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * 对象工具类
 * 基于Hutool的ObjectUtil进行扩展，提供对象空值判断和属性获取的增强功能
 * 主要用于避免空指针异常，提供安全的对象属性访问方式
 * 支持方法引用式的属性访问，代码更简洁、类型更安全
 * 是Optional的一种替代方案，适用于需要频繁访问对象属性的场景
 *
 * @author 秋辞未寒
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectUtils extends ObjectUtil {

    /**
     * 安全获取对象中的某个字段值
     * 如果对象不为空，则调用指定的getter方法获取字段值；否则返回null
     * 示例：ObjectUtils.notNullGetter(user, User::getName)
     * 避免了传统的if (user != null) { return user.getName(); } return null;的冗长写法
     *
     * @param <T> 对象的泛型类型
     * @param <E> 字段值的泛型类型
     * @param obj 目标对象
     * @param func 获取字段值的函数（通常是getter方法引用）
     * @return 对象字段值，如果对象或函数为空则返回null
     */
    public static <T, E> E notNullGetter(T obj, Function<T, E> func) {
        // 判断对象和函数都不为空，避免空指针异常
        // 这是防御式编程，确保方法的健壮性
        if (isNotNull(obj) && isNotNull(func)) {
            // 调用函数获取字段值
            // 使用方法引用使代码更简洁、类型更安全
            return func.apply(obj);
        }
        // 如果对象或函数为空，返回null
        return null;
    }

    /**
     * 安全获取对象中的某个字段值，提供默认值
     * 如果对象不为空，则调用指定的getter方法获取字段值；否则返回指定的默认值
     * 适用于需要确保非null返回值的场景
     *
     * @param <T> 对象的泛型类型
     * @param <E> 字段值的泛型类型
     * @param obj 目标对象
     * @param func 获取字段值的函数（通常是getter方法引用）
     * @param defaultValue 默认值，当对象或函数为空时返回
     * @return 对象字段值，如果对象或函数为空则返回默认值
     */
    public static <T, E> E notNullGetter(T obj, Function<T, E> func, E defaultValue) {
        // 判断对象和函数都不为空，避免空指针异常
        if (isNotNull(obj) && isNotNull(func)) {
            // 调用函数获取字段值
            return func.apply(obj);
        }
        // 如果对象或函数为空，返回指定的默认值
        // 确保调用方总是能得到非null的返回值
        return defaultValue;
    }

    /**
     * 安全获取对象值，提供默认值
     * 如果对象不为空则返回对象本身，否则返回指定的默认值
     * 类似于Apache Commons Lang的ObjectUtils.defaultIfNull
     * 适用于需要确保非null对象的场景
     *
     * @param <T> 对象的泛型类型
     * @param obj 目标对象
     * @param defaultValue 默认值，当对象为空时返回
     * @return 对象本身（如果不为空）或默认值（如果对象为空）
     */
    public static <T> T notNull(T obj, T defaultValue) {
        // 判断对象不为空
        if (isNotNull(obj)) {
            // 返回对象本身
            return obj;
        }
        // 如果对象为空，返回指定的默认值
        return defaultValue;
    }

}

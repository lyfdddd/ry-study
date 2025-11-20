// 基于Hutool的对象工具类，提供空值安全的属性访问方法
// 扩展Hutool的ObjectUtil，提供增强的对象操作能力
package org.dromara.common.core.utils;

// Hutool对象工具类（提供基础对象操作）
// ObjectUtil是Hutool封装的对象操作工具类
import cn.hutool.core.util.ObjectUtil;
// Lombok注解：设置构造方法访问级别为私有，防止类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;

// Java函数式接口（用于方法引用和Lambda表达式）
import java.util.function.Function;

/**
 * 对象工具类
 * 基于Hutool的ObjectUtil进行扩展，提供对象空值判断和属性获取的增强功能
 * 主要用于避免空指针异常，提供安全的对象属性访问方式
 * 支持方法引用式的属性访问，代码更简洁、类型更安全
 * 是Optional的一种替代方案，适用于需要频繁访问对象属性的场景
 * 是RuoYi-Vue-Plus中处理对象空值的核心工具类
 *
 * @author 秋辞未寒
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// access = AccessLevel.PRIVATE确保构造方法私有，无法从外部创建对象
// 符合工具类的设计模式，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// 对象工具类，继承Hutool的ObjectUtil
public class ObjectUtils extends ObjectUtil {

    /**
     * 安全获取对象中的某个字段值
     * 如果对象不为空，则调用指定的getter方法获取字段值；否则返回null
     * 示例：ObjectUtils.notNullGetter(user, User::getName)
     * 避免了传统的if (user != null) { return user.getName(); } return null;的冗长写法
     * 使用方法引用使代码更简洁、类型更安全
     *
     * @param <T> 对象的泛型类型
     * @param <E> 字段值的泛型类型
     * @param obj 目标对象
     * @param func 获取字段值的函数（通常是getter方法引用）
     * @return 对象字段值，如果对象或函数为空则返回null
     */
    // 静态方法，方便全局调用，无需创建对象
    // 使用泛型支持任意类型的对象和字段值
    // 安全获取对象中的某个字段值
    public static <T, E> E notNullGetter(T obj, Function<T, E> func) {
        // 判断对象和函数都不为空，避免空指针异常
        // 这是防御式编程，确保方法的健壮性
        // 先判断obj不为null，再判断func不为null，防止空指针异常
        // 判断对象和函数都不为空，避免空指针异常
        if (isNotNull(obj) && isNotNull(func)) {
            // 调用函数获取字段值
            // 使用方法引用使代码更简洁、类型更安全
            // 例如：User::getName，编译期检查类型安全
            // 调用函数获取字段值，使用方法引用使代码更简洁
            return func.apply(obj);
        }
        // 如果对象或函数为空，返回null
        // 避免抛出异常，让调用方自行处理null值
        // 如果对象或函数为空，返回null
        return null;
    }

    /**
     * 安全获取对象中的某个字段值，提供默认值
     * 如果对象不为空，则调用指定的getter方法获取字段值；否则返回指定的默认值
     * 适用于需要确保非null返回值的场景
     * 避免调用方重复进行null值判断
     *
     * @param <T> 对象的泛型类型
     * @param <E> 字段值的泛型类型
     * @param obj 目标对象
     * @param func 获取字段值的函数（通常是getter方法引用）
     * @param defaultValue 默认值，当对象或函数为空时返回
     * @return 对象字段值，如果对象或函数为空则返回默认值
     */
    // 静态方法，方便全局调用
    // 使用泛型支持任意类型的对象和字段值
    // 安全获取对象中的某个字段值，提供默认值
    public static <T, E> E notNullGetter(T obj, Function<T, E> func, E defaultValue) {
        // 判断对象和函数都不为空，避免空指针异常
        // 这是防御式编程，确保方法的健壮性
        // 判断对象和函数都不为空，避免空指针异常
        if (isNotNull(obj) && isNotNull(func)) {
            // 调用函数获取字段值
            // 使用方法引用使代码更简洁、类型更安全
            // 调用函数获取字段值
            return func.apply(obj);
        }
        // 如果对象或函数为空，返回指定的默认值
        // 确保调用方总是能得到非null的返回值
        // 避免调用方重复进行null值判断
        // 如果对象或函数为空，返回指定的默认值
        return defaultValue;
    }

    /**
     * 安全获取对象值，提供默认值
     * 如果对象不为空则返回对象本身，否则返回指定的默认值
     * 类似于Apache Commons Lang的ObjectUtils.defaultIfNull
     * 适用于需要确保非null对象的场景
     * 是Optional.orElse()的简化版本
     *
     * @param <T> 对象的泛型类型
     * @param obj 目标对象
     * @param defaultValue 默认值，当对象为空时返回
     * @return 对象本身（如果不为空）或默认值（如果对象为空）
     */
    // 静态方法，方便全局调用
    // 使用泛型支持任意类型的对象
    // 安全获取对象值，提供默认值
    public static <T> T notNull(T obj, T defaultValue) {
        // 判断对象不为空
        // 调用父类ObjectUtil.isNotNull()方法判断
        // 判断对象不为空
        if (isNotNull(obj)) {
            // 返回对象本身
            // 对象不为null，直接返回
            // 返回对象本身
            return obj;
        }
        // 如果对象为空，返回指定的默认值
        // 确保调用方总是能得到非null的对象
        // 避免空指针异常
        // 如果对象为空，返回指定的默认值
        return defaultValue;
    }

}

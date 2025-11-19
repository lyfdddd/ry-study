package org.dromara.common.core.utils.reflect;

import cn.hutool.core.util.ReflectUtil;
import org.dromara.common.core.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 反射工具类
 * 提供调用getter/setter方法、访问私有变量、调用私有方法、获取泛型类型Class、被AOP过的真实类等工具函数
 * 基于Hutool的ReflectUtil进行扩展，提供更便捷的对象属性操作方法
 *
 * @author Lion Li
 */
// 抑制原始类型警告，因为反射操作可能涉及原始类型
@SuppressWarnings("rawtypes")
// Lombok注解：生成私有构造方法，防止工具类被实例化
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils extends ReflectUtil {

    /**
     * Setter方法前缀
     * 用于拼接方法名，如：setName
     */
    private static final String SETTER_PREFIX = "set";

    /**
     * Getter方法前缀
     * 用于拼接方法名，如：getName
     */
    private static final String GETTER_PREFIX = "get";

    /**
     * 调用Getter方法获取对象属性值
     * 支持多级属性访问，如：user.dept.name
     * 通过反射调用getXxx()方法链式获取嵌套对象的属性值
     *
     * @param <E> 返回值的泛型类型
     * @param obj 目标对象
     * @param propertyName 属性名，支持点号分隔的多级属性（如"user.dept.name"）
     * @return 属性值，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public static <E> E invokeGetter(Object obj, String propertyName) {
        // 初始化对象为传入的对象
        Object object = obj;
        // 按点号分割属性名，支持多级属性访问，如：user.dept.name
        for (String name : StringUtils.split(propertyName, ".")) {
            // 如果当前对象为null，直接返回null，避免NPE
            if (object == null) {
                return null;
            }
            // 拼接Getter方法名，首字母大写，如：getName
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            // 使用Hutool的ReflectUtil调用方法，获取属性值
            object = invoke(object, getterMethodName);
        }
        // 返回最终获取的属性值，强制类型转换
        return (E) object;
    }

    /**
     * 调用Setter方法设置对象属性值
     * 支持多级属性设置，如：user.dept.name = "张三"
     * 通过反射调用setXxx()方法链式设置嵌套对象的属性值
     *
     * @param <E> 属性值的泛型类型
     * @param obj 目标对象
     * @param propertyName 属性名，支持点号分隔的多级属性（如"user.dept.name"）
     * @param value 要设置的属性值
     */
    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        // 初始化对象为传入的对象
        Object object = obj;
        // 按点号分割属性名，支持多级属性设置
        String[] names = StringUtils.split(propertyName, ".");
        for (int i = 0; i < names.length; i++) {
            // 如果不是最后一级，需要逐级获取对象
            if (i < names.length - 1) {
                // 拼接Getter方法名，获取中间对象
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
                object = invoke(object, getterMethodName);
                // 如果中间对象为null，无法继续设置，直接返回
                if (object == null) {
                    return;
                }
            } else {
                // 最后一级，拼接Setter方法名，设置属性值
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
                // 通过方法名获取Method对象
                Method method = getMethodByName(object.getClass(), setterMethodName);
                // 调用Setter方法设置值
                invoke(object, method, value);
            }
        }
    }

}

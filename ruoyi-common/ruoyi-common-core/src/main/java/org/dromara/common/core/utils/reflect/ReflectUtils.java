// 反射工具类包声明，属于核心工具包
package org.dromara.common.core.utils.reflect;

// 导入Hutool反射工具类，提供基础反射操作能力
import cn.hutool.core.util.ReflectUtil;
// 导入字符串工具类，用于字符串分割、首字母大写等操作
import org.dromara.common.core.utils.StringUtils;
// Lombok注解：控制构造方法访问级别
import lombok.AccessLevel;
// Lombok注解：自动生成构造方法
import lombok.NoArgsConstructor;

// 导入Java反射核心类，用于获取方法对象
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
// 继承Hutool的ReflectUtil，复用其基础反射能力
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
    // 抑制未检查类型转换警告，反射操作需要强制类型转换
    @SuppressWarnings("unchecked")
    // 泛型方法，返回值类型由调用方指定
    public static <E> E invokeGetter(Object obj, String propertyName) {
        // 初始化对象为传入的对象，作为遍历起点
        Object object = obj;
        // 按点号分割属性名，支持多级属性访问，如：user.dept.name
        // StringUtils.split返回字符串数组，支持空值处理
        for (String name : StringUtils.split(propertyName, ".")) {
            // 如果当前对象为null，直接返回null，避免NPE（空指针异常）
            if (object == null) {
                return null;
            }
            // 拼接Getter方法名，首字母大写，如：getName
            // StringUtils.capitalize将首字母转换为大写
            String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(name);
            // 使用Hutool的ReflectUtil调用方法，获取属性值
            // invoke方法会反射调用对象的getter方法
            object = invoke(object, getterMethodName);
        }
        // 返回最终获取的属性值，强制类型转换为泛型类型E
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
    // 泛型方法，支持设置任意类型的属性值
    public static <E> void invokeSetter(Object obj, String propertyName, E value) {
        // 初始化对象为传入的对象，作为遍历起点
        Object object = obj;
        // 按点号分割属性名，支持多级属性设置
        // StringUtils.split返回字符串数组
        String[] names = StringUtils.split(propertyName, ".");
        // 遍历属性名数组，逐级获取或设置对象
        for (int i = 0; i < names.length; i++) {
            // 如果不是最后一级，需要逐级获取对象
            if (i < names.length - 1) {
                // 拼接Getter方法名，获取中间对象
                String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(names[i]);
                // 反射调用getter方法获取中间对象
                object = invoke(object, getterMethodName);
                // 如果中间对象为null，无法继续设置，直接返回
                // 避免NPE，保证方法健壮性
                if (object == null) {
                    return;
                }
            } else {
                // 最后一级，拼接Setter方法名，设置属性值
                String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(names[i]);
                // 通过方法名获取Method对象，用于反射调用
                Method method = getMethodByName(object.getClass(), setterMethodName);
                // 调用Setter方法设置值，完成属性赋值
                invoke(object, method, value);
            }
        }
    }

}

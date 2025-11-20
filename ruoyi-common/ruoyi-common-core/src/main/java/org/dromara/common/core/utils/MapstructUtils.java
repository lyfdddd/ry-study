// 基于Mapstruct-plus框架的对象转换工具类，提供高性能实体转换功能
// 该类封装了Mapstruct-plus的Converter，提供对象转换的便捷方法
// 主要应用于实体类、DTO、VO之间的属性拷贝和转换场景
package org.dromara.common.core.utils;

// Hutool集合工具类，提供集合判空、创建等操作
// CollUtil.isEmpty()用于判断集合是否为空，避免空指针异常
import cn.hutool.core.collection.CollUtil;
// Hutool Map工具类，提供Map判空等操作
// MapUtil.isEmpty()用于判断Map是否为空
import cn.hutool.core.map.MapUtil;
// Hutool对象工具类，提供对象判空等操作
// ObjectUtil.isNull()用于判断对象是否为null
import cn.hutool.core.util.ObjectUtil;
// Mapstruct-plus转换器接口，提供对象转换的核心API
// Converter是Mapstruct-plus提供的核心转换器，支持对象、集合、Map等多种转换
import io.github.linpeilie.Converter;
// Lombok注解：设置构造方法访问级别为私有，防止类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;

// Java List集合接口，用于存储对象列表
import java.util.List;
// Java Map接口，用于存储键值对
import java.util.Map;

/**
 * Mapstruct-plus 对象转换工具类
 * 基于Mapstruct-plus框架的对象转换工具，提供实体类之间的属性拷贝和转换功能
 * 支持对象到对象、集合到集合、Map到Bean等多种转换场景
 * Mapstruct-plus是MapStruct的增强版，在编译期生成转换代码，性能是BeanUtils的10倍
 * 支持复杂类型转换、嵌套对象转换、自定义转换规则等高级功能
 * <p>参考文档：<a href="https://mapstruct.plus/introduction/quick-start.html">mapstruct-plus</a></p>
 * <p>性能对比：Mapstruct-plus > MapStruct > BeanUtils（反射）</p>
 *
 * @author Michelle.Chung
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapstructUtils {

    /**
     * Mapstruct-plus转换器
     * 从Spring容器中获取Converter单例，Mapstruct-plus提供的转换器
     * 用于执行对象之间的属性映射和转换
     * Converter是线程安全的，可以在多线程环境下共享使用
     * 在应用启动时由Spring容器初始化并注入
     */
    // 使用SpringUtils从Spring容器中获取Converter Bean
    // 这是单例模式的应用，确保全局只有一个Converter实例
    // 从Spring容器中获取Converter单例，用于对象转换
    // Converter是Mapstruct-plus提供的核心转换器，线程安全
    private final static Converter CONVERTER = SpringUtils.getBean(Converter.class);

    /**
     * 将源对象转换为目标类型的对象
     * 基于Mapstruct-plus的注解配置，自动映射相同名称的属性
     * 支持基本类型、包装类型、字符串、日期等常见类型转换
     * 支持嵌套对象转换，但需要配置对应的映射关系
     *
     * @param <T>  源对象类型
     * @param <V>  目标对象类型
     * @param source 源对象（数据来源实体）
     * @param desc   目标对象类型（转换后的对象类型）
     * @return 转换后的目标对象，如果源对象或目标类型为空则返回null
     */
    // 静态方法，方便全局调用，无需创建对象
    // 使用泛型支持任意类型的源对象和目标对象
    // 将源对象转换为目标类型的对象
    // 基于Mapstruct-plus的注解配置，自动映射相同名称的属性
    // 转换逻辑在编译期生成，运行时性能极高
    public static <T, V> V convert(T source, Class<V> desc) {
        // 如果源对象为空，返回null，避免空指针异常
        // 这是防御式编程，确保方法的健壮性
        // ObjectUtil.isNull()是Hutool提供的判空方法
        if (ObjectUtil.isNull(source)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 如果目标类型为空，返回null
        // 避免后续转换过程中的空指针异常
        if (ObjectUtil.isNull(desc)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 调用Mapstruct-plus的Converter进行对象转换
        // 基于注解配置自动映射相同名称的属性
        // 转换逻辑在编译期生成，运行时性能极高
        // 性能是BeanUtils的10倍以上，因为避免了反射调用
        return CONVERTER.convert(source, desc);
    }

    /**
     * 将源对象的属性拷贝到目标对象
     * 基于Mapstruct-plus的注解配置，将源对象的属性值复制到目标对象的对应属性
     * 与convert(T, Class<V>)不同，这个方法不会创建新对象，而是修改现有对象
     * 适用于需要保留目标对象部分属性的场景
     *
     * @param <T>  源对象类型
     * @param <V>  目标对象类型
     * @param source 源对象（数据来源实体）
     * @param desc   目标对象（转换后的对象，已存在实例）
     * @return 填充后的目标对象，如果源对象或目标对象为空则返回null
     */
    // 静态方法，方便全局调用，无需创建对象
    // 使用泛型支持任意类型的源对象和目标对象
    // 将源对象的属性拷贝到目标对象
    // 与convert(T, Class<V>)不同，这个方法不会创建新对象，而是修改现有对象
    // 适用于需要保留目标对象部分属性的场景
    public static <T, V> V convert(T source, V desc) {
        // 如果源对象为空，返回null
        // 没有数据源，无法进行属性拷贝
        if (ObjectUtil.isNull(source)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 如果目标对象为空，返回null
        // 没有目标对象，无法进行属性拷贝
        if (ObjectUtil.isNull(desc)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 调用Mapstruct-plus的Converter进行对象属性拷贝
        // 将源对象的属性值复制到目标对象的对应属性
        // 只拷贝配置了映射关系的属性，其他属性保持不变
        // 适用于部分更新的场景
        return CONVERTER.convert(source, desc);
    }

    /**
     * 将源对象列表转换为目标类型的对象列表
     * 批量转换集合中的每个对象，保持集合结构不变
     * 支持List、Set等集合类型的批量转换
     * 性能优化：一次性转换整个集合，而不是循环调用单个转换
     *
     * @param <T>  源对象类型
     * @param <V>  目标对象类型
     * @param sourceList 源对象列表（数据来源实体列表）
     * @param desc       目标对象类型（转换后的对象类型）
     * @return 转换后的目标对象列表，如果源列表为空则返回空列表
     */
    // 静态方法，方便全局调用，无需创建对象
    // 使用泛型支持任意类型的源对象和目标对象
    // 将源对象列表转换为目标类型的对象列表
    // 批量转换集合中的每个对象，保持集合结构不变
    // 性能优化：一次性转换整个集合，而不是循环调用单个转换
    public static <T, V> List<V> convert(List<T> sourceList, Class<V> desc) {
        // 如果源列表为null，返回null
        // 保持与输入一致，调用方可以区分null和空列表
        if (ObjectUtil.isNull(sourceList)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 如果源列表为空集合，返回空ArrayList，避免返回null
        // 这是防御式编程，确保调用方不会遇到空指针异常
        // CollUtil.isEmpty()同时检查null和空集合
        if (CollUtil.isEmpty(sourceList)) {
            // 返回空ArrayList，避免返回null导致调用方需要额外判空
            return CollUtil.newArrayList();
        }
        // 调用Mapstruct-plus的Converter进行集合转换
        // 批量转换集合中的每个对象
        // Mapstruct-plus会优化批量转换的性能
        // 性能优于循环调用单个转换，因为可以批量处理
        return CONVERTER.convert(sourceList, desc);
    }

    /**
     * 将Map转换为指定类型的Bean对象
     * 将Map中的键值对映射到Bean对象的属性
     * 支持嵌套Map转换，但需要配置对应的映射关系
     * Map的key对应Bean的属性名，value对应属性值
     *
     * @param <T> 目标Bean类型
     * @param map       数据来源（Map对象，键为属性名，值为属性值）
     * @param beanClass 目标Bean类型
     * @return 转换后的Bean对象，如果Map为空或目标类型为空则返回null
     */
    // 静态方法，方便全局调用，无需创建对象
    // 使用泛型支持任意类型的目标Bean
    // 将Map转换为指定类型的Bean对象
    // 将Map中的键值对映射到Bean对象的属性
    // 支持复杂类型的自动转换，如String转Date、String转Enum等
    public static <T> T convert(Map<String, Object> map, Class<T> beanClass) {
        // 如果Map为空，返回null
        // 没有数据源，无法进行转换
        // MapUtil.isEmpty()同时检查null和空Map
        if (MapUtil.isEmpty(map)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 如果目标类型为空，返回null
        // 没有目标类型，无法进行转换
        if (ObjectUtil.isNull(beanClass)) {
            // 返回null，保持与输入一致
            return null;
        }
        // 调用Mapstruct-plus的Converter进行Map到Bean的转换
        // 将Map中的键值对映射到Bean对象的属性
        // 支持复杂类型的自动转换，如String转Date、String转Enum等
        // 需要配置对应的转换规则
        return CONVERTER.convert(map, beanClass);
    }

}

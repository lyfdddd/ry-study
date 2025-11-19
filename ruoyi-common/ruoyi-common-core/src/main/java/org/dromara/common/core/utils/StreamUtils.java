// 基于Java 8 Stream API的增强工具类，提供集合处理的常用操作
package org.dromara.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Stream流工具类
 * 基于Java 8 Stream API的增强工具类，提供集合过滤、查找、转换、分组等常用操作
 * 封装了Stream API的复杂用法，简化集合处理逻辑
 * 提供类型安全的集合操作，避免空指针异常
 * 支持复杂的分组、映射、合并等高级操作
 * 所有方法都经过空值处理，确保健壮性
 *
 * @author Lion Li
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtils {

    /**
     * 过滤集合中的元素
     * 根据指定的条件过滤集合，返回符合条件的元素列表
     * 支持复杂的过滤条件，如对象属性判断、字符串匹配等
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要过滤的集合
     * @param function 过滤条件（Predicate函数）
     * @return 过滤后的元素列表，如果集合为空返回空列表
     */
    public static <E> List<E> filter(Collection<E> collection, Predicate<E> function) {
        // 如果集合为空，直接返回空列表，避免空指针异常
        // 这是防御式编程，确保方法的健壮性
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        // 使用Stream API进行过滤操作
        // stream()创建流，filter应用过滤条件，collect收集结果
        return collection.stream()
            .filter(function)
            // 注意：此处不要使用 .toList() 新语法，因为返回的是不可变List，会导致序列化问题
            // 使用Collectors.toList()返回可变的ArrayList，支持序列化和后续修改
            .collect(Collectors.toList());
    }

    /**
     * 找到集合中满足条件的第一个元素
     * 返回Optional包装的结果，避免空指针异常
     * 适用于需要明确处理找不到情况的场景
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要查询的集合
     * @param function 过滤条件（Predicate函数）
     * @return 找到符合条件的第一个元素，没有则返回 Optional.empty()
     */
    public static <E> Optional<E> findFirst(Collection<E> collection, Predicate<E> function) {
        // 如果集合为空，直接返回Optional.empty()
        // Optional避免空指针异常，是Java 8推荐的空值处理方式
        if (CollUtil.isEmpty(collection)) {
            return Optional.empty();
        }
        // 使用Stream API查找第一个符合条件的元素
        // findFirst()返回Optional，明确表示可能找不到
        return collection.stream()
            .filter(function)
            .findFirst();
    }

    /**
     * 找到集合中满足条件的第一个元素值
     * 直接返回值，如果不存在则返回null
     * 适用于简化代码，不需要明确处理找不到情况的场景
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要查询的集合
     * @param function 过滤条件（Predicate函数）
     * @return 找到符合条件的第一个元素，没有则返回 null
     */
    public static <E> E findFirstValue(Collection<E> collection, Predicate<E> function) {
        // 调用findFirst方法，如果不存在则返回null
        // orElse(null)将Optional为空的情况转换为null
        return findFirst(collection,function).orElse(null);
    }

    /**
     * 找到集合中任意一个满足条件的元素
     * 与findFirst不同，findAny在并行流中可能返回不同的结果
     * 在串行流中与findFirst效果相同，但在并行流中性能更好
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要查询的集合
     * @param function 过滤条件（Predicate函数）
     * @return 找到符合条件的任意一个元素，没有则返回 Optional.empty()
     */
    public static <E> Optional<E> findAny(Collection<E> collection, Predicate<E> function) {
        // 如果集合为空，直接返回Optional.empty()
        if (CollUtil.isEmpty(collection)) {
            return Optional.empty();
        }
        // 使用Stream API查找任意一个符合条件的元素
        // findAny()在并行流中性能优于findFirst()
        return collection.stream()
            .filter(function)
            .findAny();
    }

    /**
     * 找到集合中任意一个满足条件的元素值
     * 直接返回值，如果不存在则返回null
     * 适用于并行流场景，性能优于findFirstValue
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要查询的集合
     * @param function 过滤条件（Predicate函数）
     * @return 找到符合条件的任意一个元素，没有则返回null
     */
    public static <E> E findAnyValue(Collection<E> collection, Predicate<E> function) {
        // 调用findAny方法，如果不存在则返回null
        // 在并行流中性能优于findFirstValue
        return findAny(collection,function).orElse(null);
    }

    /**
     * 将集合中的元素拼接成字符串
     * 使用默认分隔符（逗号）进行拼接
     * 常用于将ID列表、名称列表等转换为字符串
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要转化的集合
     * @param function 将元素转换为字符串的函数
     * @return 拼接后的字符串
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function) {
        // 调用重载方法，使用默认分隔符
        // StringUtils.SEPARATOR是逗号，是最常用的分隔符
        return join(collection, function, StringUtils.SEPARATOR);
    }

    /**
     * 将集合中的元素拼接成字符串
     * 使用指定分隔符进行拼接
     * 支持自定义分隔符，如"|"、"-"等
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要转化的集合
     * @param function 将元素转换为字符串的函数
     * @param delimiter 拼接分隔符
     * @return 拼接后的字符串
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function, CharSequence delimiter) {
        // 如果集合为空，返回空字符串
        // 避免返回null，确保调用方不会遇到空指针异常
        if (CollUtil.isEmpty(collection)) {
            return StringUtils.EMPTY;
        }
        // 使用Stream API进行映射和拼接
        // map将元素转换为字符串，filter过滤null值，joining使用指定分隔符拼接
        return collection.stream()
            .map(function) // 将元素转换为字符串
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.joining(delimiter)); // 使用指定分隔符拼接
    }

    /**
     * 对集合中的元素进行排序
     * 根据指定的比较器进行排序
     * 支持复杂排序规则，如多字段排序、自定义排序等
     *
     * @param <E> 集合元素的泛型类型
     * @param collection 需要排序的集合
     * @param comparing 排序比较器
     * @return 排序后的列表
     */
    public static <E> List<E> sorted(Collection<E> collection, Comparator<E> comparing) {
        // 如果集合为空，返回空列表
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        // 使用Stream API进行排序
        // filter过滤null值，sorted根据比较器排序，collect收集结果
        return collection.stream()
            .filter(Objects::nonNull) // 过滤掉null值
            .sorted(comparing) // 根据比较器排序
            // 注意：此处不要使用 .toList() 新语法，因为返回的是不可变List，会导致序列化问题
            .collect(Collectors.toList());
    }

    /**
     * 将集合转换为Map，保持value类型不变<br>
     * <B>{@code Collection<V>  ---->  Map<K,V>}</B>
     * 使用元素本身作为value，通过函数提取key
     * 适用于需要通过某个属性快速查找元素的场景
     *
     * @param <V> collection中的泛型类型
     * @param <K> map中的key类型
     * @param collection 需要转化的集合
     * @param key 从元素中提取key的函数
     * @return 转化后的map，如果key重复则保留第一个
     */
    public static <V, K> Map<K, V> toIdentityMap(Collection<V> collection, Function<V, K> key) {
        // 如果集合为空，返回空Map
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        // 使用Stream API转换为Map
        // toMap的三个参数：key提取函数、value映射函数（这里是元素本身）、key冲突处理函数
        return collection.stream()
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.toMap(key, Function.identity(), (l, r) -> l)); // key重复时保留第一个
    }

    /**
     * 将Collection转化为Map（value类型与collection的泛型不同）<br>
     * <B>{@code Collection<E> -----> Map<K,V>  }</B>
     * 分别通过函数提取key和value
     * 适用于需要转换元素类型的场景
     *
     * @param <E> collection中的泛型类型
     * @param <K> map中的key类型
     * @param <V> map中的value类型
     * @param collection 需要转化的集合
     * @param key 从元素中提取key的函数
     * @param value 从元素中提取value的函数
     * @return 转化后的map，如果key重复则保留第一个
     */
    public static <E, K, V> Map<K, V> toMap(Collection<E> collection, Function<E, K> key, Function<E, V> value) {
        // 如果集合为空，返回空Map
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        // 使用Stream API转换为Map
        // 分别通过函数提取key和value，key冲突时保留第一个
        return collection.stream()
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.toMap(key, value, (l, r) -> l)); // key重复时保留第一个
    }

    /**
     * 将Map中的数据作为新Map的value，key保持不变
     * 通过函数重新计算value值
     * 适用于需要转换Map value类型的场景
     *
     * @param <K> map中的key类型
     * @param <E> 原始map中的value类型
     * @param <V> 新map中的value类型
     * @param map 需要处理的map
     * @param take 取值函数，根据key和原始value计算新value
     * @return 新的map
     */
    public static <K, E, V> Map<K, V> toMap(Map<K, E> map, BiFunction<K, E, V> take) {
        // 如果Map为空，返回空Map
        if (CollUtil.isEmpty(map)) {
            return MapUtil.newHashMap();
        }
        // 调用toMap方法，将Map的entrySet转换为新的Map
        // 使用BiFunction根据key和原始value计算新value
        return toMap(map.entrySet(), Map.Entry::getKey, entry -> take.apply(entry.getKey(), entry.getValue()));
    }

    /**
     * 将集合按照指定规则分类成Map<br>
     * <B>{@code Collection<E> -------> Map<K,List<E>> } </B>
     * 相同key的元素会被分到同一个List中
     * 适用于按某个属性分组的场景，如按部门分组用户
     *
     * @param <E> collection中的泛型类型
     * @param <K> map中的key类型
     * @param collection 需要分类的集合
     * @param key 分类规则（从元素中提取key的函数）
     * @return 分类后的map，使用LinkedHashMap保持插入顺序
     */
    public static <E, K> Map<K, List<E>> groupByKey(Collection<E> collection, Function<E, K> key) {
        // 如果集合为空，返回空Map
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        // 使用Stream API进行分组
        // groupingBy的三个参数：分类函数、Map工厂（LinkedHashMap保持顺序）、收集器（toList）
        return collection.stream()
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.toList())); // 使用LinkedHashMap保持顺序
    }

    /**
     * 将集合按照两个规则分类成双层Map<br>
     * <B>{@code Collection<E>  --->  Map<T,Map<U,List<E>>> } </B>
     * 先按第一个规则分组，再在每个组内按第二个规则分组
     * 适用于复杂的二级分组场景，如按部门分组再按职位分组
     *
     * @param <E> 集合元素类型
     * @param <K> 第一个map中的key类型
     * @param <U> 第二个map中的key类型
     * @param collection 需要分类的集合
     * @param key1 第一个分类规则（从元素中提取key的函数）
     * @param key2 第二个分类规则（从元素中提取key的函数）
     * @return 分类后的双层map，使用LinkedHashMap保持插入顺序
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(Collection<E> collection, Function<E, K> key1, Function<E, U> key2) {
        // 如果集合为空，返回空Map
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        // 使用Stream API进行双层分组
        // 外层groupingBy按key1分组，内层groupingBy按key2分组，都使用LinkedHashMap保持顺序
        return collection.stream()
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.groupingBy(key1, LinkedHashMap::new, Collectors.groupingBy(key2, LinkedHashMap::new, Collectors.toList())));
    }

    /**
     * 将集合按照两个规则分类成双层Map（内层Map的value为单个元素）<br>
     * <B>{@code Collection<E>  --->  Map<T,Map<U,E>> } </B>
     * 适用于需要唯一键的场景，内层Map的key不能重复
     * 如按部门分组用户，每个职位只保留一个用户
     *
     * @param <E> collection中的泛型类型
     * @param <T> 第一个map中的key类型
     * @param <U> 第二个map中的key类型
     * @param collection 需要分类的集合
     * @param key1 第一个分类规则（从元素中提取key的函数）
     * @param key2 第二个分类规则（从元素中提取key的函数）
     * @return 分类后的双层map，如果key重复则保留第一个，使用LinkedHashMap保持顺序
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(Collection<E> collection, Function<E, T> key1, Function<E, U> key2) {
        // 如果集合为空，返回空Map
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        // 使用Stream API进行双层分组，内层使用toMap
        // 外层groupingBy按key1分组，内层toMap按key2映射，key冲突时保留第一个
        return collection.stream()
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.groupingBy(key1, LinkedHashMap::new, Collectors.toMap(key2, Function.identity(), (l, r) -> l)));
    }

    /**
     * 将集合转化为List，但两者的泛型不同<br>
     * <B>{@code Collection<E>  ------>  List<T> } </B>
     * 通过函数将每个元素转换为目标类型
     * 常用于DTO转换，如将DO列表转换为DTO列表
     *
     * @param <E> collection中的泛型类型
     * @param <T> List中的泛型类型
     * @param collection 需要转化的集合
     * @param function 将元素从E类型转换为T类型的函数
     * @return 转化后的List，使用ArrayList实现
     */
    public static <E, T> List<T> toList(Collection<E> collection, Function<E, T> function) {
        // 如果集合为空，返回空列表
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        // 使用Stream API进行映射和收集
        // map进行类型转换，filter过滤null值，collect收集为List
        return collection.stream()
            .map(function) // 将元素转换为目标类型
            .filter(Objects::nonNull) // 过滤掉null值
            // 注意：此处不要使用 .toList() 新语法，因为返回的是不可变List，会导致序列化问题
            .collect(Collectors.toList());
    }

    /**
     * 将集合转化为Set，但两者的泛型不同<br>
     * <B>{@code Collection<E>  ------>  Set<T> } </B>
     * 通过函数将每个元素转换为目标类型，自动去重
     * 常用于提取唯一值，如提取用户ID集合
     *
     * @param <E> collection中的泛型类型
     * @param <T> Set中的泛型类型
     * @param collection 需要转化的集合
     * @param function 将元素从E类型转换为T类型的函数
     * @return 转化后的Set，使用HashSet实现
     */
    public static <E, T> Set<T> toSet(Collection<E> collection, Function<E, T> function) {
        // 如果集合为空，返回空集合
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newHashSet();
        }
        // 使用Stream API进行映射和收集
        // map进行类型转换，filter过滤null值，collect收集为Set（自动去重）
        return collection.stream()
            .map(function) // 将元素转换为目标类型
            .filter(Objects::nonNull) // 过滤掉null值
            .collect(Collectors.toSet()); // 收集为Set，自动去重
    }


    /**
     * 合并两个相同key类型的Map
     * 通过合并函数将两个Map的value合并为新的value
     * 适用于需要合并两个数据源的场景，如合并两个统计结果
     *
     * @param <K> map中的key类型
     * @param <X> 第一个map的value类型
     * @param <Y> 第二个map的value类型
     * @param <V> 最终map的value类型
     * @param map1 第一个需要合并的map
     * @param map2 第二个需要合并的map
     * @param merge 合并函数，将两个value合并成最终的类型，注意value可能为空的情况
     * @return 合并后的map
     */
    public static <K, X, Y, V> Map<K, V> merge(Map<K, X> map1, Map<K, Y> map2, BiFunction<X, Y, V> merge) {
        // 如果两个map都为空，返回空map
        if (CollUtil.isEmpty(map1) && CollUtil.isEmpty(map2)) {
            return MapUtil.newHashMap();
        } else if (CollUtil.isEmpty(map1)) {
            // 如果map1为空，直接处理map2
            // 将map2的value通过merge函数转换，map1的value为null
            return toMap(map2.entrySet(), Map.Entry::getKey, entry -> merge.apply(null, entry.getValue()));
        } else if (CollUtil.isEmpty(map2)) {
            // 如果map2为空，直接处理map1
            // 将map1的value通过merge函数转换，map2的value为null
            return toMap(map1.entrySet(), Map.Entry::getKey, entry -> merge.apply(entry.getValue(), null));
        }
        // 获取所有key的集合
        // 使用HashSet自动去重，确保处理所有存在的key
        Set<K> keySet = new HashSet<>();
        keySet.addAll(map1.keySet());
        keySet.addAll(map2.keySet());
        // 合并所有key，对每个key应用合并函数
        // 使用toMap将keySet转换为Map，通过merge函数合并两个value
        return toMap(keySet, key -> key, key -> merge.apply(map1.get(key), map2.get(key)));
    }

}

// 定义缓存操作工具类所在的包路径，属于common-redis模块
package org.dromara.common.redis.utils;

// Lombok注解：设置构造方法访问级别为PRIVATE，防止实例化
import lombok.AccessLevel;
// Lombok注解：生成无参构造方法
import lombok.NoArgsConstructor;
// Spring工具类，用于获取Spring容器中的Bean
import org.dromara.common.core.utils.SpringUtils;
// Spring Cache接口，提供缓存操作API
import org.springframework.cache.Cache;
// Spring CacheManager接口，管理缓存实例
import org.springframework.cache.CacheManager;

/**
 * Spring Cache操作工具类
 * 基于Spring Cache抽象层封装，提供统一的缓存操作入口
 * 支持多种缓存实现（Redis、Caffeine、EhCache等）
 *
 * @author Michelle.Chung
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// 压制警告：泛型未检查
@SuppressWarnings(value = {"unchecked"})
public class CacheUtils {

    // Spring CacheManager单例，从Spring容器获取
    // 用于管理所有缓存实例（如：userCache、dictCache等）
    private static final CacheManager CACHE_MANAGER = SpringUtils.getBean(CacheManager.class);

    /**
     * 获取缓存值（带类型转换）
     * 从指定缓存组中获取key对应的值
     *
     * @param cacheNames 缓存组名称（如：userCache、dictCache）
     * @param key        缓存key（可以是任意类型，会自动转换为String）
     * @param <T>        返回值类型
     * @return 缓存值，不存在返回null
     */
    public static <T> T get(String cacheNames, Object key) {
        // 获取缓存实例
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        // 获取ValueWrapper（Spring Cache的包装类）
        Cache.ValueWrapper wrapper = cache.get(key);
        // 如果wrapper不为null，返回包装的值（需要强制类型转换）
        // 如果wrapper为null，返回null
        return wrapper != null ? (T) wrapper.get() : null;
    }

    /**
     * 保存缓存值（覆盖模式）
     * 如果key已存在则覆盖，不存在则创建
     *
     * @param cacheNames 缓存组名称（如：userCache、dictCache）
     * @param key        缓存key
     * @param value      缓存值（任意类型）
     */
    public static void put(String cacheNames, Object key, Object value) {
        // 获取缓存实例
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        // 将key-value放入缓存
        cache.put(key, value);
    }

    /**
     * 删除缓存值（单个key）
     * 从指定缓存组中删除指定key
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存key
     */
    public static void evict(String cacheNames, Object key) {
        // 获取缓存实例
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        // 删除指定key
        cache.evict(key);
    }

    /**
     * 清空缓存值（整个缓存组）
     * 删除指定缓存组中的所有key
     *
     * @param cacheNames 缓存组名称
     */
    public static void clear(String cacheNames) {
        // 获取缓存实例
        Cache cache = CACHE_MANAGER.getCache(cacheNames);
        // 清空整个缓存组
        cache.clear();
    }

}

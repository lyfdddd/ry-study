// Apache License 2.0 版权声明
// 原始代码来自Redisson项目，由Nikita Koksharov编写
/**
 * Copyright (c) 2013-2021 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// 定义Spring Cache管理器实现类所在的包路径，属于common-redis模块
package org.dromara.common.redis.manager;

// Redis工具类，用于获取Redisson客户端
import org.dromara.common.redis.utils.RedisUtils;
// Redisson Map接口，用于创建普通缓存
import org.redisson.api.RMap;
// Redisson MapCache接口，用于创建带过期时间的缓存
import org.redisson.api.RMapCache;
// Redisson缓存配置类
import org.redisson.spring.cache.CacheConfig;
// Redisson缓存实现类
import org.redisson.spring.cache.RedissonCache;
// Spring Boot Duration解析工具类
import org.springframework.boot.convert.DurationStyle;
// Spring Cache接口
import org.springframework.cache.Cache;
// Spring CacheManager接口
import org.springframework.cache.CacheManager;
// Spring事务感知缓存装饰器
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
// Spring字符串工具类
import org.springframework.util.StringUtils;

// Java集合接口
import java.util.Collection;
// Java集合工具类
import java.util.Collections;
// Java Map接口
import java.util.Map;
// Java并发Map实现
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Spring CacheManager实现类
 * 基于Redisson实例提供缓存管理功能
 * <p>
 * 修改 RedissonSpringCacheManager 源码
 * 重写 cacheName 处理方法 支持多参数（如：cacheName#TTL#MaxIdleTime#MaxSize#Local）
 * 支持本地缓存（Caffeine）和事务感知
 *
 * @author Nikita Koksharov
 * @author RuoYi-Plus 团队（修改版）
 */
// 压制警告：泛型未检查
@SuppressWarnings("unchecked")
public class PlusSpringCacheManager implements CacheManager {

    // 是否动态创建缓存（true=缓存不存在时自动创建；false=只使用预定义的缓存）
    private boolean dynamic = true;

    // 是否允许存储null值（true=允许；false=禁止）
    private boolean allowNullValues = true;

    // 是否启用事务感知（true=在事务提交后才执行put/evict操作）
    private boolean transactionAware = true;

    // 缓存配置映射（cacheName -> CacheConfig）
    Map<String, CacheConfig> configMap = new ConcurrentHashMap<>();
    
    // 缓存实例映射（cacheName -> Cache）
    ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();

    /**
     * 创建由Redisson实例提供的CacheManager
     * 默认构造方法
     */
    public PlusSpringCacheManager() {
        // 空构造方法，使用默认值初始化
    }


    /**
     * 定义是否允许存储null值
     * <p>
     * 默认为 <code>true</code>
     *
     * @param allowNullValues 如果为<code>true</code>则允许存储null值
     */
    public void setAllowNullValues(boolean allowNullValues) {
        // 设置allowNullValues属性
        this.allowNullValues = allowNullValues;
    }

    /**
     * 定义缓存是否感知Spring管理的事务
     * 如果为{@code true}，put/evict操作仅在成功事务的after-commit阶段执行
     * <p>
     * 默认为 <code>false</code>
     *
     * @param transactionAware 如果为<code>true</code>则缓存感知事务
     */
    public void setTransactionAware(boolean transactionAware) {
        // 设置transactionAware属性
        this.transactionAware = transactionAware;
    }

    /**
     * 定义'固定'缓存名称
     * 对于未定义的名称，不会动态创建新的缓存实例
     * <p>
     * `null`参数设置为动态模式
     *
     * @param names 缓存名称集合
     */
    public void setCacheNames(Collection<String> names) {
        // 如果names不为null，预创建缓存并关闭动态模式
        if (names != null) {
            // 遍历所有缓存名称，预创建缓存
            for (String name : names) {
                getCache(name);
            }
            // 关闭动态模式
            dynamic = false;
        } else {
            // 开启动态模式
            dynamic = true;
        }
    }

    /**
     * 设置按缓存名称映射的缓存配置
     *
     * @param config 配置对象映射
     */
    public void setConfig(Map<String, ? extends CacheConfig> config) {
        // 将配置映射转换为ConcurrentHashMap
        this.configMap = (Map<String, CacheConfig>) config;
    }

    /**
     * 创建默认缓存配置
     * @return 默认CacheConfig对象
     */
    protected CacheConfig createDefaultConfig() {
        // 返回新的CacheConfig实例
        return new CacheConfig();
    }

    /**
     * 获取缓存实例（核心方法）
     * 重写cacheName支持多参数格式：cacheName#TTL#MaxIdleTime#MaxSize#Local
     *
     * @param name 缓存名称（支持多参数格式）
     * @return Cache实例
     */
    @Override
    public Cache getCache(String name) {
        // 重写cacheName支持多参数，使用#分隔
        // 例如：userCache#10m#5m#1000#1 表示：缓存名=userCache, TTL=10分钟, MaxIdleTime=5分钟, MaxSize=1000, 启用本地缓存
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        // 第一个参数是缓存名称
        name = array[0];

        // 从实例映射中获取缓存
        Cache cache = instanceMap.get(name);
        // 如果缓存已存在，直接返回
        if (cache != null) {
            return cache;
        }
        // 如果不是动态模式，返回null（不创建新缓存）
        if (!dynamic) {
            return cache;
        }

        // 获取缓存配置
        CacheConfig config = configMap.get(name);
        // 如果配置不存在，创建默认配置
        if (config == null) {
            config = createDefaultConfig();
            // 将默认配置放入配置映射
            configMap.put(name, config);
        }

        // 解析多参数配置
        // 第二个参数是TTL（生存时间）
        if (array.length > 1) {
            // 解析Duration并转换为毫秒
            config.setTTL(DurationStyle.detectAndParse(array[1]).toMillis());
        }
        // 第三个参数是MaxIdleTime（最大空闲时间）
        if (array.length > 2) {
            config.setMaxIdleTime(DurationStyle.detectAndParse(array[2]).toMillis());
        }
        // 第四个参数是MaxSize（最大容量）
        if (array.length > 3) {
            config.setMaxSize(Integer.parseInt(array[3]));
        }
        // 第五个参数是Local（是否启用本地缓存，1=启用，0=不启用）
        int local = 1;
        if (array.length > 4) {
            local = Integer.parseInt(array[4]);
        }

        // 如果TTL、MaxIdleTime、MaxSize都为0，创建普通Map缓存
        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {
            return createMap(name, config, local);
        }

        // 否则创建MapCache缓存（支持过期时间和容量限制）
        return createMapCache(name, config, local);
    }

    /**
     * 创建普通Map缓存（无过期时间）
     * @param name 缓存名称
     * @param config 缓存配置
     * @param local 是否启用本地缓存（1=启用，0=不启用）
     * @return Cache实例
     */
    private Cache createMap(String name, CacheConfig config, int local) {
        // 获取Redisson Map实例
        RMap<Object, Object> map = RedisUtils.getClient().getMap(name);

        // 创建RedissonCache实例
        Cache cache = new RedissonCache(map, allowNullValues);
        // 如果启用本地缓存，使用CaffeineCacheDecorator包装
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        // 如果启用事务感知，使用TransactionAwareCacheDecorator包装
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        // 将缓存放入实例映射（如果已存在则返回已存在的）
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        return cache;
    }

    /**
     * 创建MapCache缓存（支持过期时间和容量限制）
     * @param name 缓存名称
     * @param config 缓存配置
     * @param local 是否启用本地缓存（1=启用，0=不启用）
     * @return Cache实例
     */
    private Cache createMapCache(String name, CacheConfig config, int local) {
        // 获取Redisson MapCache实例（支持过期时间）
        RMapCache<Object, Object> map = RedisUtils.getClient().getMapCache(name);

        // 创建RedissonCache实例
        Cache cache = new RedissonCache(map, config, allowNullValues);
        // 如果启用本地缓存，使用CaffeineCacheDecorator包装
        if (local == 1) {
            cache = new CaffeineCacheDecorator(name, cache);
        }
        // 如果启用事务感知，使用TransactionAwareCacheDecorator包装
        if (transactionAware) {
            cache = new TransactionAwareCacheDecorator(cache);
        }
        // 将缓存放入实例映射（如果已存在则返回已存在的）
        Cache oldCache = instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            // 如果是新缓存，设置最大容量
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    /**
     * 获取所有缓存名称
     * @return 缓存名称集合（不可修改）
     */
    @Override
    public Collection<String> getCacheNames() {
        // 返回配置映射中的所有key（缓存名称）
        return Collections.unmodifiableSet(configMap.keySet());
    }


}

package org.dromara.common.satoken.core.dao;

import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject;
import cn.dev33.satoken.util.SaFoxUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.dromara.common.redis.utils.RedisUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sa-Token持久层接口(使用框架自带RedisUtils实现 协议统一)
 * <p>
 * 采用 caffeine + redis 多级缓存 优化并发查询效率
 * <p>
 * SaTokenDaoBySessionFollowObject 是 SaTokenDao 子集简化了session方法处理
 *
 * @author Lion Li
 */
// 实现SaTokenDaoBySessionFollowObject接口，提供Token的持久化存储能力
// 采用Caffeine本地缓存 + Redis远程缓存的二级缓存架构，提升查询性能
public class PlusSaTokenDao implements SaTokenDaoBySessionFollowObject {

    // 定义Caffeine本地缓存实例，用于缓存热点数据
    // expireAfterWrite: 5秒过期，防止数据不一致
    // initialCapacity: 初始容量100，减少扩容开销
    // maximumSize: 最大容量1000，防止内存溢出
    private static final Cache<String, Object> CAFFEINE = Caffeine.newBuilder()
        // 设置最后一次写入或访问后经过固定时间过期
        .expireAfterWrite(5, TimeUnit.SECONDS)
        // 初始的缓存空间大小
        .initialCapacity(100)
        // 缓存的最大条数
        .maximumSize(1000)
        .build();

    /**
     * 获取Value，如无返空
     * @param key 缓存键
     * @return 字符串值
     */
    @Override
    public String get(String key) {
        // 先从Caffeine缓存获取，如果没有则调用RedisUtils从Redis获取
        // 使用lambda表达式实现缓存加载逻辑
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        return (String) o;
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间（秒）
     */
    @Override
    public void set(String key, String value, long timeout) {
        // 如果超时时间无效，直接返回
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            // 永不过期，直接存储到Redis
            RedisUtils.setCacheObject(key, value);
        } else {
            // 设置过期时间存储到Redis
            RedisUtils.setCacheObject(key, value, Duration.ofSeconds(timeout));
        }
        // 写入Redis后，使Caffeine缓存失效，保证数据一致性
        CAFFEINE.invalidate(key);
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     * @param key 缓存键
     * @param value 新值
     */
    @Override
    public void update(String key, String value) {
        // 只有key存在时才更新
        if (RedisUtils.hasKey(key)) {
            // 使用true参数保持原有过期时间
            RedisUtils.setCacheObject(key, value, true);
            // 更新后使Caffeine缓存失效
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除Value
     * @param key 缓存键
     */
    @Override
    public void delete(String key) {
        // 从Redis删除key
        if (RedisUtils.deleteObject(key)) {
            // 删除成功后使Caffeine缓存失效
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     * @param key 缓存键
     * @return 剩余时间（秒）
     */
    @Override
    public long getTimeout(String key) {
        // 从Redis获取TTL（毫秒）
        long timeout = RedisUtils.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        // Redis返回毫秒，Sa-Token需要秒，转换时+1补偿精度损失
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     * @param key 缓存键
     * @param timeout 新的过期时间（秒）
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        // 使用RedisUtils设置过期时间
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }


    /**
     * 获取Object，如无返空
     * @param key 缓存键
     * @return 对象
     */
    @Override
    public Object getObject(String key) {
        // 先从Caffeine缓存获取，如果没有则调用RedisUtils从Redis获取
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        return o;
    }

    /**
     * 获取 Object (指定反序列化类型)，如无返空
     *
     * @param key 缓存键
     * @param classType 目标类型
     * @param <T> 泛型类型
     * @return 指定类型的对象
     */
    @SuppressWarnings("unchecked cast")
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        // 先从Caffeine缓存获取，如果没有则调用RedisUtils从Redis获取
        Object o = CAFFEINE.get(key, k -> RedisUtils.getCacheObject(key));
        // 强制类型转换
        return (T) o;
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     * @param key 缓存键
     * @param object 缓存对象
     * @param timeout 过期时间（秒）
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        // 如果超时时间无效，直接返回
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            // 永不过期，直接存储到Redis
            RedisUtils.setCacheObject(key, object);
        } else {
            // 设置过期时间存储到Redis
            RedisUtils.setCacheObject(key, object, Duration.ofSeconds(timeout));
        }
        // 写入Redis后，使Caffeine缓存失效
        CAFFEINE.invalidate(key);
    }

    /**
     * 更新Object (过期时间不变)
     * @param key 缓存键
     * @param object 新对象
     */
    @Override
    public void updateObject(String key, Object object) {
        // 只有key存在时才更新
        if (RedisUtils.hasKey(key)) {
            // 使用true参数保持原有过期时间
            RedisUtils.setCacheObject(key, object, true);
            // 更新后使Caffeine缓存失效
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除Object
     * @param key 缓存键
     */
    @Override
    public void deleteObject(String key) {
        // 从Redis删除key
        if (RedisUtils.deleteObject(key)) {
            // 删除成功后使Caffeine缓存失效
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     * @param key 缓存键
     * @return 剩余时间（秒）
     */
    @Override
    public long getObjectTimeout(String key) {
        // 从Redis获取TTL（毫秒）
        long timeout = RedisUtils.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        // Redis返回毫秒，Sa-Token需要秒，转换时+1补偿精度损失
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     * @param key 缓存键
     * @param timeout 新的过期时间（秒）
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        // 使用RedisUtils设置过期时间
        RedisUtils.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 搜索数据
     * @param prefix 键前缀
     * @param keyword 搜索关键词
     * @param start 起始位置
     * @param size 返回数量
     * @param sortType 是否排序
     * @return 匹配的键列表
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        // 构建搜索模式：prefix*keyword*
        String keyStr = prefix + "*" + keyword + "*";
        // 先从Caffeine缓存获取搜索结果
        return (List<String>) CAFFEINE.get(keyStr, k -> {
            // 如果缓存中没有，调用RedisUtils.keys()获取匹配的键集合
            Collection<String> keys = RedisUtils.keys(keyStr);
            // 转换为List
            List<String> list = new ArrayList<>(keys);
            // 使用Sa-Token的工具类进行分页和排序
            return SaFoxUtil.searchList(list, start, size, sortType);
        });
    }
}

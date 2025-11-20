// 定义PlusSmsDao类的包路径
package org.dromara.common.sms.core.dao;

// 引入全局常量类
import org.dromara.common.core.constant.GlobalConstants;
// 引入Redis工具类
import org.dromara.common.redis.utils.RedisUtils;
// 引入Sms4j框架的SmsDao接口
import org.dromara.sms4j.api.dao.SmsDao;

// 引入Duration类，用于设置缓存时间
import java.time.Duration;

/**
 * SmsDao缓存配置实现类 (使用框架自带RedisUtils实现，协议统一)
 * <p>主要用于短信重试和拦截的缓存管理，基于Redis实现</p>
 * <p>实现了Sms4j框架的SmsDao接口，提供短信相关的缓存操作</p>
 *
 * @author Feng
 */
// PlusSmsDao类，实现SmsDao接口
public class PlusSmsDao implements SmsDao {

    /**
     * 存储键值对到Redis，带过期时间
     * 使用GLOBAL_REDIS_KEY作为key前缀，避免key冲突
     *
     * @param key       缓存键
     * @param value     缓存值
     * @param cacheTime 缓存时间（单位：秒）
     */
    @Override
    public void set(String key, Object value, long cacheTime) {
        // 调用RedisUtils设置缓存，使用Duration.ofSeconds将秒转换为Duration对象
        RedisUtils.setCacheObject(GlobalConstants.GLOBAL_REDIS_KEY + key, value, Duration.ofSeconds(cacheTime));
    }

    /**
     * 存储键值对到Redis，使用默认过期时间
     * 使用GLOBAL_REDIS_KEY作为key前缀，避免key冲突
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Override
    public void set(String key, Object value) {
        // 调用RedisUtils设置缓存，true表示使用默认过期时间
        RedisUtils.setCacheObject(GlobalConstants.GLOBAL_REDIS_KEY + key, value, true);
    }

    /**
     * 从Redis读取缓存值
     * 使用GLOBAL_REDIS_KEY作为key前缀
     *
     * @param key 缓存键
     * @return 缓存值，如果key不存在返回null
     */
    @Override
    public Object get(String key) {
        // 调用RedisUtils获取缓存对象
        return RedisUtils.getCacheObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 根据key移除缓存
     * <p>从Redis中删除指定key的缓存</p>
     *
     * @param key 缓存键
     * @return 被删除的value，如果key不存在返回false
     * @author :Wind
     */
    @Override
    public Object remove(String key) {
        // 调用RedisUtils删除缓存对象
        return RedisUtils.deleteObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 清空所有短信相关的缓存
     * 删除以GLOBAL_REDIS_KEY + "sms:*"为pattern的所有key
     */
    @Override
    public void clean() {
        // 调用RedisUtils批量删除匹配pattern的key
        RedisUtils.deleteKeys(GlobalConstants.GLOBAL_REDIS_KEY + "sms:*");
    }

}

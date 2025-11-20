// 定义授权状态缓存类的包路径
package org.dromara.common.social.utils;

// Lombok注解，生成包含所有字段的构造函数
import lombok.AllArgsConstructor;
// 引入JustAuth的授权状态缓存接口
import me.zhyd.oauth.cache.AuthStateCache;
// 引入全局常量类
import org.dromara.common.core.constant.GlobalConstants;
// 引入Redis工具类
import org.dromara.common.redis.utils.RedisUtils;

// 引入Duration类，用于设置缓存时间
import java.time.Duration;

/**
 * 授权状态缓存实现类
 * 基于Redis实现OAuth2授权状态的存储和管理
 * 用于存储和验证OAuth2授权过程中的state参数，防止CSRF攻击
 */
// Lombok注解，生成包含所有字段的构造函数
@AllArgsConstructor
// 授权状态缓存实现类，实现AuthStateCache接口
public class AuthRedisStateCache implements AuthStateCache {

    /**
     * 存入缓存，使用默认超时时间（3分钟）
     * 用于存储OAuth2授权过程中的state状态值
     *
     * @param key   缓存key，通常是OAuth2的state参数
     * @param value 缓存内容，通常是随机生成的state值
     */
    @Override
    public void cache(String key, String value) {
        // 授权超时时间默认三分钟，使用SOCIAL_AUTH_CODE_KEY作为key前缀
        RedisUtils.setCacheObject(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key, value, Duration.ofMinutes(3));
    }

    /**
     * 存入缓存，指定超时时间（毫秒）
     * 用于存储OAuth2授权过程中的state状态值，可自定义过期时间
     *
     * @param key     缓存key，通常是OAuth2的state参数
     * @param value   缓存内容，通常是随机生成的state值
     * @param timeout 指定缓存过期时间（单位：毫秒）
     */
    @Override
    public void cache(String key, String value, long timeout) {
        // 将毫秒转换为Duration对象，设置缓存过期时间
        RedisUtils.setCacheObject(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key, value, Duration.ofMillis(timeout));
    }

    /**
     * 获取缓存内容
     * 从Redis中获取存储的state值，用于验证OAuth2回调的合法性
     *
     * @param key 缓存key，通常是OAuth2的state参数
     * @return 缓存内容，如果key不存在或已过期返回null
     */
    @Override
    public String get(String key) {
        // 从Redis获取缓存对象，使用SOCIAL_AUTH_CODE_KEY作为key前缀
        return RedisUtils.getCacheObject(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key);
    }

    /**
     * 检查key是否存在且未过期
     * 用于验证OAuth2回调中的state参数是否有效
     *
     * @param key 缓存key，通常是OAuth2的state参数
     * @return true：存在key且value没过期；false：key不存在或已过期
     */
    @Override
    public boolean containsKey(String key) {
        // 检查Redis中是否存在该key，使用SOCIAL_AUTH_CODE_KEY作为key前缀
        return RedisUtils.hasKey(GlobalConstants.SOCIAL_AUTH_CODE_KEY + key);
    }
}

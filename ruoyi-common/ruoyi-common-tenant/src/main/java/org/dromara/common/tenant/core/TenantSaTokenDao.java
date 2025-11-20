package org.dromara.common.tenant.core;

import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.satoken.core.dao.PlusSaTokenDao;

import java.time.Duration;
import java.util.List;

/**
 * SaToken 认证数据持久层 适配多租户
 *
 * @author Lion Li
 */
// 继承PlusSaTokenDao，扩展多租户支持
public class TenantSaTokenDao extends PlusSaTokenDao {

    // 重写get方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public String get(String key) {
        return super.get(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    // 重写set方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public void set(String key, String value, long timeout) {
        super.set(GlobalConstants.GLOBAL_REDIS_KEY + key, value, timeout);
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     */
    // 重写update方法，先获取原有过期时间，再使用原过期时间重新设置值
    @Override
    public void update(String key, String value) {
        // 获取当前key的剩余过期时间（秒）
        long expire = getTimeout(key);
        // -2 = 无此键，如果key不存在直接返回
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        // 使用原有过期时间重新设置值
        this.set(key, value, expire);
    }

    /**
     * 删除Value
     */
    // 重写delete方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public void delete(String key) {
        super.delete(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    // 重写getTimeout方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public long getTimeout(String key) {
        return super.getTimeout(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    // 重写updateTimeout方法，支持设置为永久有效
    @Override
    public void updateTimeout(String key, long timeout) {
        // 判断是否想要设置为永久有效（NEVER_EXPIRE = -1）
        if (timeout == NEVER_EXPIRE) {
            // 获取当前key的剩余过期时间
            long expire = getTimeout(key);
            // 如果其已经被设置为永久，则不作任何处理
            if (expire == NEVER_EXPIRE) {
                // 已经是永久有效，无需处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次，使用原值和永久标志
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        // 非永久有效，使用RedisUtils直接设置过期时间
        RedisUtils.expire(GlobalConstants.GLOBAL_REDIS_KEY + key, Duration.ofSeconds(timeout));
    }


    /**
     * 获取Object，如无返空
     */
    // 重写getObject方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public Object getObject(String key) {
        return super.getObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 获取 Object (指定反序列化类型)，如无返空
     *
     * @param key 键名称
     * @return object
     */
    // 重写getObject方法，支持指定反序列化类型，在key前添加全局Redis Key前缀
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        return super.getObject(GlobalConstants.GLOBAL_REDIS_KEY + key, classType);
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     */
    // 重写setObject方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public void setObject(String key, Object object, long timeout) {
        super.setObject(GlobalConstants.GLOBAL_REDIS_KEY + key, object, timeout);
    }

    /**
     * 更新Object (过期时间不变)
     */
    // 重写updateObject方法，先获取原有过期时间，再使用原过期时间重新设置对象
    @Override
    public void updateObject(String key, Object object) {
        // 获取当前key的剩余过期时间（秒）
        long expire = getObjectTimeout(key);
        // -2 = 无此键，如果key不存在直接返回
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        // 使用原有过期时间重新设置对象
        this.setObject(key, object, expire);
    }

    /**
     * 删除Object
     */
    // 重写deleteObject方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public void deleteObject(String key) {
        super.deleteObject(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     */
    // 重写getObjectTimeout方法，在key前添加全局Redis Key前缀，实现租户隔离
    @Override
    public long getObjectTimeout(String key) {
        return super.getObjectTimeout(GlobalConstants.GLOBAL_REDIS_KEY + key);
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     */
    // 重写updateObjectTimeout方法，支持设置为永久有效
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        // 判断是否想要设置为永久有效（NEVER_EXPIRE = -1）
        if (timeout == NEVER_EXPIRE) {
            // 获取当前key的剩余过期时间
            long expire = getObjectTimeout(key);
            // 如果其已经被设置为永久，则不作任何处理
            if (expire == NEVER_EXPIRE) {
                // 已经是永久有效，无需处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次，使用原对象和永久标志
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        // 非永久有效，使用RedisUtils直接设置过期时间
        RedisUtils.expire(GlobalConstants.GLOBAL_REDIS_KEY + key, Duration.ofSeconds(timeout));
    }

    /**
     * 搜索数据
     */
    // 重写searchData方法，在prefix前添加全局Redis Key前缀，实现租户隔离
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        return super.searchData(GlobalConstants.GLOBAL_REDIS_KEY + prefix, keyword, start, size, sortType);
    }
}

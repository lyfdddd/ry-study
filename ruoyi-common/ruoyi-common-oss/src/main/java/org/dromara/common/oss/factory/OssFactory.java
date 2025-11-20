// 定义OSS工厂类的包路径
package org.dromara.common.oss.factory;

// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 缓存名称常量
import org.dromara.common.core.constant.CacheNames;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// JSON工具类，用于JSON字符串与对象互转
import org.dromara.common.json.utils.JsonUtils;
// OSS常量接口
import org.dromara.common.oss.constant.OssConstant;
// OSS客户端核心类
import org.dromara.common.oss.core.OssClient;
// OSS自定义异常类
import org.dromara.common.oss.exception.OssException;
// OSS配置属性类
import org.dromara.common.oss.properties.OssProperties;
// 缓存工具类
import org.dromara.common.redis.utils.CacheUtils;
// Redis工具类
import org.dromara.common.redis.utils.RedisUtils;

// Java Map集合
import java.util.Map;
// Java并发Map，线程安全
import java.util.concurrent.ConcurrentHashMap;
// Java可重入锁
import java.util.concurrent.locks.ReentrantLock;

/**
 * OSS文件上传工厂类
 * 负责创建和管理OSS客户端实例，采用单例模式+工厂模式
 * 支持多租户隔离，每个租户有独立的OSS客户端实例
 * 使用ConcurrentHashMap缓存客户端实例，提升性能
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成slf4j日志对象
@Slf4j
// OSS工厂类
public class OssFactory {

    /**
     * OSS客户端实例缓存
     * 使用ConcurrentHashMap实现线程安全的缓存
     * key格式：租户ID:配置KEY 或 配置KEY（单租户场景）
     */
    private static final Map<String, OssClient> CLIENT_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 可重入锁
     * 用于双重检查锁定模式，防止并发创建相同key的客户端实例
     */
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 获取默认OSS客户端实例
     * 从Redis读取默认配置KEY，然后调用instance(configKey)获取实例
     *
     * @return OSS客户端实例
     * @throws OssException 如果未配置默认OSS类型
     */
    public static OssClient instance() {
        // 从Redis获取默认OSS配置KEY
        String configKey = RedisUtils.getCacheObject(OssConstant.DEFAULT_CONFIG_KEY);
        // 如果未配置默认KEY，抛出异常
        if (StringUtils.isEmpty(configKey)) {
            throw new OssException("文件存储服务类型无法找到!");
        }
        // 调用重载方法获取实例
        return instance(configKey);
    }

    /**
     * 根据配置KEY获取OSS客户端实例
     * 支持多租户隔离，使用租户ID作为key前缀
     * 采用双重检查锁定模式确保线程安全
     *
     * @param configKey 配置KEY
     * @return OSS客户端实例
     * @throws OssException 如果配置信息不存在
     */
    public static OssClient instance(String configKey) {
        // 从缓存获取JSON格式的配置信息
        String json = CacheUtils.get(CacheNames.SYS_OSS_CONFIG, configKey);
        // 如果配置不存在，抛出异常
        if (json == null) {
            throw new OssException("系统异常, '" + configKey + "'配置信息不存在!");
        }
        // 将JSON字符串解析为OssProperties对象
        OssProperties properties = JsonUtils.parseObject(json, OssProperties.class);
        
        // 构建缓存key，使用租户标识避免多个租户相同key实例覆盖
        String key = configKey;
        // 如果配置了租户ID，将租户ID作为前缀
        if (StringUtils.isNotBlank(properties.getTenantId())) {
            key = properties.getTenantId() + ":" + configKey;
        }
        
        // 从缓存获取客户端实例
        OssClient client = CLIENT_CACHE.get(key);
        // 客户端不存在或配置不相同则重新构建
        if (client == null || !client.checkPropertiesSame(properties)) {
            // 获取锁，确保只有一个线程创建实例
            LOCK.lock();
            try {
                // 双重检查，防止其他线程已创建实例
                client = CLIENT_CACHE.get(key);
                // 再次检查，确保实例确实需要创建
                if (client == null || !client.checkPropertiesSame(properties)) {
                    // 创建新的OSS客户端实例并放入缓存
                    CLIENT_CACHE.put(key, new OssClient(configKey, properties));
                    // 记录日志
                    log.info("创建OSS实例 key => {}", configKey);
                    // 返回新创建的实例
                    return CLIENT_CACHE.get(key);
                }
            } finally {
                // 释放锁
                LOCK.unlock();
            }
        }
        // 返回缓存中的实例
        return client;
    }

}

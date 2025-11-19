// 定义Redis工具类所在的包路径，属于common-redis模块
package org.dromara.common.redis.utils;

// Lombok注解：设置构造方法访问级别为PRIVATE，防止实例化
import lombok.AccessLevel;
// Lombok注解：生成无参构造方法
import lombok.NoArgsConstructor;
// Spring工具类，用于获取Spring容器中的Bean
import org.dromara.common.core.utils.SpringUtils;
// Redisson客户端接口，提供Redis操作API
import org.redisson.api.*;
// Redisson扫描选项配置类
import org.redisson.api.options.KeysScanOptions;

// Java时间Duration类，用于设置过期时间
import java.time.Duration;
// Java集合接口
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Java函数式接口
import java.util.function.Consumer;
// Java Stream API
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Redis工具类
 * 基于Redisson客户端封装，提供缓存、限流、发布订阅、原子操作等通用功能
 * 所有方法均为静态方法，方便全局调用
 *
 * @author Lion Li
 * @version 3.1.0 新增
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// 压制警告：泛型未检查、原始类型
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisUtils {

    // Redisson客户端单例，从Spring容器获取
    private static final RedissonClient CLIENT = SpringUtils.getBean(RedissonClient.class);

    /**
     * 限流方法（默认超时0秒）
     * 基于Redis令牌桶算法实现分布式限流
     *
     * @param key          限流key（如：user:123:api）
     * @param rateType     限流类型（OVERALL全局限流、PER_CLIENT单客户端限流）
     * @param rate         速率（每秒生成令牌数）
     * @param rateInterval 速率间隔（秒）
     * @return 剩余令牌数，-1表示获取失败（被限流）
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        // 调用重载方法，默认超时0秒
        return rateLimiter(key, rateType, rate, rateInterval, 0);
    }

    /**
     * 限流方法（带超时时间）
     * 基于Redis令牌桶算法实现分布式限流
     *
     * @param key          限流key（如：user:123:api）
     * @param rateType     限流类型（OVERALL全局限流、PER_CLIENT单客户端限流）
     * @param rate         速率（每秒生成令牌数）
     * @param rateInterval 速率间隔（秒）
     * @param timeout      超时时间（秒）
     * @return 剩余令牌数，-1表示获取失败（被限流）
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval, int timeout) {
        // 获取或创建限流器
        RRateLimiter rateLimiter = CLIENT.getRateLimiter(key);
        // 设置限流速率（类型、速率、间隔、超时）
        rateLimiter.trySetRate(rateType, rate, Duration.ofSeconds(rateInterval), Duration.ofSeconds(timeout));
        // 尝试获取一个令牌
        if (rateLimiter.tryAcquire()) {
            // 获取成功，返回剩余令牌数
            return rateLimiter.availablePermits();
        } else {
            // 获取失败，返回-1（被限流）
            return -1L;
        }
    }

    /**
     * 获取Redisson客户端实例
     * 用于需要直接操作Redisson高级API的场景
     *
     * @return RedissonClient实例
     */
    public static RedissonClient getClient() {
        // 返回静态客户端实例
        return CLIENT;
    }

    /**
     * 发布通道消息（带回调）
     * 发布消息到Redis频道并执行自定义回调
     *
     * @param channelKey 通道key（如：order:notify）
     * @param msg        发送数据（任意类型）
     * @param consumer   自定义处理回调
     */
    public static <T> void publish(String channelKey, T msg, Consumer<T> consumer) {
        // 获取或创建Topic
        RTopic topic = CLIENT.getTopic(channelKey);
        // 发布消息
        topic.publish(msg);
        // 执行回调函数
        consumer.accept(msg);
    }

    /**
     * 发布消息到指定的频道
     * 发布消息到Redis频道（无回调）
     *
     * @param channelKey 通道key（如：order:notify）
     * @param msg        发送数据（任意类型）
     */
    public static <T> void publish(String channelKey, T msg) {
        // 获取或创建Topic
        RTopic topic = CLIENT.getTopic(channelKey);
        // 发布消息
        topic.publish(msg);
    }

    /**
     * 订阅通道接收消息
     * 订阅Redis频道并处理接收到的消息
     *
     * @param channelKey 通道key（如：order:notify）
     * @param clazz      消息类型Class
     * @param consumer   自定义处理回调
     */
    public static <T> void subscribe(String channelKey, Class<T> clazz, Consumer<T> consumer) {
        // 获取或创建Topic
        RTopic topic = CLIENT.getTopic(channelKey);
        // 添加监听器，接收消息时触发回调
        topic.addListener(clazz, (channel, msg) -> consumer.accept(msg));
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等（无过期时间）
     * 如果key已存在则覆盖
     *
     * @param key   缓存的键值
     * @param value 缓存的值（任意类型）
     */
    public static <T> void setCacheObject(final String key, final T value) {
        // 调用重载方法，不保留TTL
        setCacheObject(key, value, false);
    }

    /**
     * 缓存基本的对象，保留当前对象TTL有效期
     * 如果key已存在且isSaveTtl=true，则保留原有TTL
     *
     * @param key       缓存的键值
     * @param value     缓存的值（任意类型）
     * @param isSaveTtl 是否保留TTL有效期（例如：set之前ttl剩余90秒，set之后还是为90秒）
     * @since Redis 6.X以上使用setAndKeepTTL，兼容5.X方案
     */
    public static <T> void setCacheObject(final String key, final T value, final boolean isSaveTtl) {
        // 获取Bucket对象（Redis字符串）
        RBucket<T> bucket = CLIENT.getBucket(key);
        if (isSaveTtl) {
            try {
                // Redis 6.0+支持setAndKeepTTL
                bucket.setAndKeepTTL(value);
            } catch (Exception e) {
                // 兼容Redis 5.X：先获取剩余TTL，再set并设置过期时间
                long timeToLive = bucket.remainTimeToLive();
                if (timeToLive == -1) {
                    // 没有设置过期时间，直接set
                    bucket.set(value);
                } else {
                    // 保留原有TTL
                    bucket.set(value, Duration.ofMillis(timeToLive));
                }
            }
        } else {
            // 不保留TTL，直接覆盖
            bucket.set(value);
        }
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等（带过期时间）
     *
     * @param key      缓存的键值
     * @param value    缓存的值（任意类型）
     * @param duration 过期时间（Duration）
     */
    public static <T> void setCacheObject(final String key, final T value, final Duration duration) {
        // 获取Bucket对象
        RBucket<T> bucket = CLIENT.getBucket(key);
        // 设置值和过期时间
        bucket.set(value, duration);
    }

    /**
     * 如果不存在则设置，并返回true；如果已存在则返回false（SETNX命令）
     * 原子操作，用于分布式锁等场景
     *
     * @param key      缓存的键值
     * @param value    缓存的值（任意类型）
     * @param duration 过期时间
     * @return true=设置成功；false=设置失败（key已存在）
     */
    public static <T> boolean setObjectIfAbsent(final String key, final T value, final Duration duration) {
        // 获取Bucket对象
        RBucket<T> bucket = CLIENT.getBucket(key);
        // 执行SETNX操作（Redisson封装）
        return bucket.setIfAbsent(value, duration);
    }

    /**
     * 如果存在则设置，并返回true；如果不存在则返回false
     * 原子操作
     *
     * @param key      缓存的键值
     * @param value    缓存的值（任意类型）
     * @param duration 过期时间
     * @return true=设置成功；false=设置失败（key不存在）
     */
    public static <T> boolean setObjectIfExists(final String key, final T value, final Duration duration) {
        // 获取Bucket对象
        RBucket<T> bucket = CLIENT.getBucket(key);
        // 执行SETXX操作（Redisson封装）
        return bucket.setIfExists(value, duration);
    }

    /**
     * 注册对象监听器
     * 当key被修改、删除、过期时触发监听器
     * <p>
     * key监听器需开启Redis配置：`notify-keyspace-events Ex`（过期事件）
     *
     * @param key      缓存的键值
     * @param listener 监听器实现
     */
    public static <T> void addObjectListener(final String key, final ObjectListener listener) {
        // 获取Bucket对象
        RBucket<T> result = CLIENT.getBucket(key);
        // 添加监听器
        result.addListener(listener);
    }

    /**
     * 设置有效时间（秒）
     *
     * @param key     Redis键
     * @param timeout 超时时间（秒）
     * @return true=设置成功；false=设置失败
     */
    public static boolean expire(final String key, final long timeout) {
        // 转换为Duration并调用重载方法
        return expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 设置有效时间
     *
     * @param key      Redis键
     * @param duration 超时时间（Duration）
     * @return true=设置成功；false=设置失败
     */
    public static boolean expire(final String key, final Duration duration) {
        // 获取Bucket对象
        RBucket rBucket = CLIENT.getBucket(key);
        // 设置过期时间
        return rBucket.expire(duration);
    }

    /**
     * 获得缓存的基本对象
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据，不存在返回null
     */
    public static <T> T getCacheObject(final String key) {
        // 获取Bucket对象
        RBucket<T> rBucket = CLIENT.getBucket(key);
        // 获取值
        return rBucket.get();
    }

    /**
     * 获得key剩余存活时间（TTL）
     *
     * @param key 缓存键值
     * @return 剩余存活时间（毫秒），-1表示永不过期，-2表示key不存在
     */
    public static <T> long getTimeToLive(final String key) {
        // 获取Bucket对象
        RBucket<T> rBucket = CLIENT.getBucket(key);
        // 获取剩余TTL
        return rBucket.remainTimeToLive();
    }

    /**
     * 删除单个对象
     *
     * @param key 缓存的键值
     * @return true=删除成功；false=删除失败（key不存在）
     */
    public static boolean deleteObject(final String key) {
        // 获取Bucket并删除
        return CLIENT.getBucket(key).delete();
    }

    /**
     * 批量删除集合对象
     * 使用Redis Pipeline提升性能
     *
     * @param collection key集合
     */
    public static void deleteObject(final Collection collection) {
        // 创建批量操作
        RBatch batch = CLIENT.createBatch();
        // 遍历集合，异步删除
        collection.forEach(t -> {
            batch.getBucket(t.toString()).deleteAsync();
        });
        // 执行批量操作
        batch.execute();
    }

    /**
     * 检查缓存对象是否存在
     *
     * @param key 缓存的键值
     * @return true=存在；false=不存在
     */
    public static boolean isExistsObject(final String key) {
        // 获取Bucket并检查是否存在
        return CLIENT.getBucket(key).isExists();
    }

    /**
     * 缓存List数据（全量）
     * 如果key已存在则覆盖
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return true=成功；false=失败
     */
    public static <T> boolean setCacheList(final String key, final List<T> dataList) {
        // 获取List对象
        RList<T> rList = CLIENT.getList(key);
        // 添加所有元素
        return rList.addAll(dataList);
    }

    /**
     * 追加缓存List数据（单个元素）
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return true=成功；false=失败
     */
    public static <T> boolean addCacheList(final String key, final T data) {
        // 获取List对象
        RList<T> rList = CLIENT.getList(key);
        // 添加单个元素
        return rList.add(data);
    }

    /**
     * 注册List监听器
     * 当List被修改时触发监听器
     * <p>
     * 需开启Redis配置：`notify-keyspace-events El`（列表事件）
     *
     * @param key      缓存的键值
     * @param listener 监听器实现
     */
    public static <T> void addListListener(final String key, final ObjectListener listener) {
        // 获取List对象
        RList<T> rList = CLIENT.getList(key);
        // 添加监听器
        rList.addListener(listener);
    }

    /**
     * 获得缓存的list对象（全部）
     *
     * @param key 缓存的键值
     * @return List数据，key不存在返回空列表
     */
    public static <T> List<T> getCacheList(final String key) {
        // 获取List对象
        RList<T> rList = CLIENT.getList(key);
        // 读取所有元素
        return rList.readAll();
    }

    /**
     * 获得缓存的list对象（范围查询）
     * 支持分页查询
     *
     * @param key  缓存的键值
     * @param form 起始下标（包含）
     * @param to   截止下标（包含）
     * @return 指定范围的List数据
     */
    public static <T> List<T> getCacheListRange(final String key, int form, int to) {
        // 获取List对象
        RList<T> rList = CLIENT.getList(key);
        // 获取范围元素
        return rList.range(form, to);
    }

    /**
     * 缓存Set数据（全量）
     * 如果key已存在则覆盖
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return true=成功；false=失败
     */
    public static <T> boolean setCacheSet(final String key, final Set<T> dataSet) {
        // 获取Set对象
        RSet<T> rSet = CLIENT.getSet(key);
        // 添加所有元素
        return rSet.addAll(dataSet);
    }

    /**
     * 追加缓存Set数据（单个元素）
     * Set自动去重
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return true=成功；false=失败（元素已存在）
     */
    public static <T> boolean addCacheSet(final String key, final T data) {
        // 获取Set对象
        RSet<T> rSet = CLIENT.getSet(key);
        // 添加元素（自动去重）
        return rSet.add(data);
    }

    /**
     * 注册Set监听器
     * 当Set被修改时触发监听器
     *
     * @param key      缓存的键值
     * @param listener 监听器实现
     */
    public static <T> void addSetListener(final String key, final ObjectListener listener) {
        // 获取Set对象
        RSet<T> rSet = CLIENT.getSet(key);
        // 添加监听器
        rSet.addListener(listener);
    }

    /**
     * 获得缓存的set（全部）
     *
     * @param key 缓存的key
     * @return Set数据，key不存在返回空Set
     */
    public static <T> Set<T> getCacheSet(final String key) {
        // 获取Set对象
        RSet<T> rSet = CLIENT.getSet(key);
        // 读取所有元素
        return rSet.readAll();
    }

    /**
     * 缓存Map数据（全量）
     * 如果key已存在则覆盖
     *
     * @param key     缓存的键值
     * @param dataMap 缓存的数据
     */
    public static <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        // 判空
        if (dataMap != null) {
            // 获取Map对象
            RMap<String, T> rMap = CLIENT.getMap(key);
            // 批量放入
            rMap.putAll(dataMap);
        }
    }

    /**
     * 注册Map监听器
     * 当Map被修改时触发监听器
     *
     * @param key      缓存的键值
     * @param listener 监听器实现
     */
    public static <T> void addMapListener(final String key, final ObjectListener listener) {
        // 获取Map对象
        RMap<String, T> rMap = CLIENT.getMap(key);
        // 添加监听器
        rMap.addListener(listener);
    }

    /**
     * 获得缓存的Map（全部）
     *
     * @param key 缓存的键值
     * @return Map数据，key不存在返回空Map
     */
    public static <T> Map<String, T> getCacheMap(final String key) {
        // 获取Map对象
        RMap<String, T> rMap = CLIENT.getMap(key);
        // 获取所有键值对
        return rMap.getAll(rMap.keySet());
    }

    /**
     * 获得缓存Map的key列表
     *
     * @param key 缓存的键值
     * @return key集合，key不存在返回空Set
     */
    public static <T> Set<String> getCacheMapKeySet(final String key) {
        // 获取Map对象
        RMap<String, T> rMap = CLIENT.getMap(key);
        // 获取所有key
        return rMap.keySet();
    }

    /**
     * 往Hash中存入数据（单个键值对）
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public static <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        // 获取Map对象
        RMap<String, T> rMap = CLIENT.getMap(key);
        // 放入键值对
        rMap.put(hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象，不存在返回null
     */
    public static <T> T getCacheMapValue(final String key, final String hKey) {
        // 获取Map对象
        RMap<String, T> rMap = CLIENT.getMap(key);
        // 获取值
        return rMap.get(hKey);
    }

    /**
     * 删除Hash中的数据（单个键值对）
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return 被删除的值，不存在返回null
     */
    public static <T> T delCacheMapValue(final String key, final String hKey) {
        // 获取Map对象
        RMap<String, T> rMap = CLIENT.getMap(key);
        // 删除并返回值
        return rMap.remove(hKey);
    }

    /**
     * 批量删除Hash中的数据
     * 使用Redis Pipeline提升性能
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     */
    public static <T> void delMultiCacheMapValue(final String key, final Set<String> hKeys) {
        // 创建批量操作
        RBatch batch = CLIENT.createBatch();
        // 获取Map的异步操作对象
        RMapAsync<String, T> rMap = batch.getMap(key);
        // 批量删除
        for (String hKey : hKeys) {
            rMap.removeAsync(hKey);
        }
        // 执行批量操作
        batch.execute();
    }

    /**
     * 批量获取Hash中的数据
     * 使用Redis Pipeline提升性能
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public static <K, V> Map<K, V> getMultiCacheMapValue(final String key, final Set<K> hKeys) {
        // 获取Map对象
        RMap<K, V> rMap = CLIENT.getMap(key);
        // 批量获取
        return rMap.getAll(hKeys);
    }

    /**
     * 设置原子值（Long类型）
     * 用于计数器、序列号生成等场景
     *
     * @param key   Redis键
     * @param value 值
     */
    public static void setAtomicValue(String key, long value) {
        // 获取原子Long对象
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        // 设置值
        atomic.set(value);
    }

    /**
     * 获取原子值
     *
     * @param key Redis键
     * @return 当前值
     */
    public static long getAtomicValue(String key) {
        // 获取原子Long对象
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        // 获取值
        return atomic.get();
    }

    /**
     * 递增原子值（+1）
     * 原子操作，线程安全
     *
     * @param key Redis键
     * @return 递增后的值
     */
    public static long incrAtomicValue(String key) {
        // 获取原子Long对象
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        // 递增并返回新值
        return atomic.incrementAndGet();
    }

    /**
     * 递减原子值（-1）
     * 原子操作，线程安全
     *
     * @param key Redis键
     * @return 递减后的值
     */
    public static long decrAtomicValue(String key) {
        // 获取原子Long对象
        RAtomicLong atomic = CLIENT.getAtomicLong(key);
        // 递减并返回新值
        return atomic.decrementAndGet();
    }

    /**
     * 获得缓存的基本对象列表（全局匹配，忽略租户，需自行拼接租户id）
     * 使用SCAN命令，避免阻塞Redis
     * <P>
     * limit-设置扫描的限制数量（默认为0，查询全部）
     * pattern-设置键的匹配模式（默认为null）
     * chunkSize-设置每次扫描的块大小（默认为0，本方法设置为1000）
     * type-设置键的类型（默认为null，查询全部类型）
     * </P>
     * @see KeysScanOptions
     * @param pattern 字符串前缀（如：user:*）
     * @return key列表
     */
    public static Collection<String> keys(final String pattern) {
        // 调用重载方法，设置chunkSize=1000
        return  keys(KeysScanOptions.defaults().pattern(pattern).chunkSize(1000));
    }

    /**
     * 通过扫描参数获取缓存的基本对象列表
     * 使用SCAN命令，避免阻塞Redis
     * @param keysScanOptions 扫描参数
     * <P>
     * limit-设置扫描的限制数量（默认为0，查询全部）
     * pattern-设置键的匹配模式（默认为null）
     * chunkSize-设置每次扫描的块大小（默认为0）
     * type-设置键的类型（默认为null，查询全部类型）
     * </P>
     * @see KeysScanOptions
     */
    public static Collection<String> keys(final KeysScanOptions keysScanOptions) {
        // 获取key的Stream流
        Stream<String> keysStream = CLIENT.getKeys().getKeysStream(keysScanOptions);
        // 收集为List并返回
        return keysStream.collect(Collectors.toList());
    }

    /**
     * 删除缓存的基本对象列表（全局匹配，忽略租户，需自行拼接租户id）
     * 使用Redis的KEYS或SCAN命令删除
     *
     * @param pattern 字符串前缀（如：user:*）
     */
    public static void deleteKeys(final String pattern) {
        // 使用Redisson的deleteByPattern方法
        CLIENT.getKeys().deleteByPattern(pattern);
    }

    /**
     * 检查redis中是否存在key
     *
     * @param key 键
     * @return true=存在；false=不存在
     */
    public static Boolean hasKey(String key) {
        // 获取Keys操作对象
        RKeys rKeys = CLIENT.getKeys();
        // 检查key是否存在（countExists返回存在的key数量）
        return rKeys.countExists(key) > 0;
    }
}

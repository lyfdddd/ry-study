// 定义发号器工具类所在的包路径，属于common-redis模块
package org.dromara.common.redis.utils;

// Hutool类型转换工具类
import cn.hutool.core.convert.Convert;
// Hutool日期格式常量类
import cn.hutool.core.date.DatePattern;
// Lombok注解：设置构造方法访问级别为PRIVATE，防止实例化
import lombok.AccessLevel;
// Lombok注解：生成无参构造方法
import lombok.NoArgsConstructor;
// Spring工具类，用于获取Spring容器中的Bean
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// Redisson ID生成器接口
import org.redisson.api.RIdGenerator;
// Redisson客户端接口
import org.redisson.api.RedissonClient;

// Java时间Duration类，用于设置过期时间
import java.time.Duration;
// Java日期类
import java.time.LocalDate;
// Java日期时间类
import java.time.LocalDateTime;
// Java日期时间格式化类
import java.time.format.DateTimeFormatter;
// Java时间访问器接口
import java.time.temporal.TemporalAccessor;

/**
 * 发号器工具类
 * 基于Redisson的分布式ID生成器，支持多种ID生成策略
 * 包括：普通递增ID、日期格式ID、时间格式ID等
 * 支持ID补位、业务前缀、过期时间等特性
 *
 * @author 秋辞未寒
 * @date 2024-12-10
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SequenceUtils {

    /**
     * 默认初始值（从1开始）
     */
    public static final long DEFAULT_INIT_VALUE = 1L;

    /**
     * 默认步长（每次递增1）
     */
    public static final long DEFAULT_STEP_VALUE = 1L;

    /**
     * 默认过期时间-天（1天）
     * 用于日期格式ID，每天重置
     */
    public static final Duration DEFAULT_EXPIRE_TIME_DAY = Duration.ofDays(1);

    /**
     * 默认过期时间-分钟（1分钟）
     * 用于时间格式ID，每分钟重置
     */
    public static final Duration DEFAULT_EXPIRE_TIME_MINUTE = Duration.ofMinutes(1);

    /**
     * 默认最小ID容量位数 - 6位数（即至少可以生成的ID为999999个）
     * 用于ID补位，确保ID长度一致
     */
    public static final int DEFAULT_MIN_ID_CAPACITY_BITS = 6;

    /**
     * Redisson客户端单例，从Spring容器获取
     * 用于创建ID生成器
     */
    private static final RedissonClient REDISSON_CLIENT = SpringUtils.getBean(RedissonClient.class);

    /**
     * 获取ID生成器（完整参数）
     * 创建或获取指定key的ID生成器，并设置初始值、步长、过期时间
     *
     * @param key        业务key（Redis key，如：order:id）
     * @param expireTime 过期时间（到期后ID从初始值重新开始）
     * @param initValue  ID初始值（必须大于0）
     * @param stepValue  ID步长（必须大于0）
     * @return ID生成器（RIdGenerator）
     */
    public static RIdGenerator getIdGenerator(String key, Duration expireTime, long initValue, long stepValue) {
        // 获取或创建ID生成器
        RIdGenerator idGenerator = REDISSON_CLIENT.getIdGenerator(key);
        // 初始值和步长不能小于等于0，使用默认值
        initValue = initValue <= 0 ? DEFAULT_INIT_VALUE : initValue;
        stepValue = stepValue <= 0 ? DEFAULT_STEP_VALUE : stepValue;
        // 设置初始值和步长（仅当生成器不存在时有效）
        idGenerator.tryInit(initValue, stepValue);
        // 设置过期时间
        idGenerator.expire(expireTime);
        return idGenerator;
    }

    /**
     * 获取ID生成器（简化参数）
     * 使用默认初始值和步长
     *
     * @param key        业务key
     * @param expireTime 过期时间
     * @return ID生成器
     */
    public static RIdGenerator getIdGenerator(String key, Duration expireTime) {
        // 调用重载方法，使用默认初始值和步长
        return getIdGenerator(key, expireTime, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE);
    }

    /**
     * 获取指定业务key的唯一id（完整参数）
     * 基于Redisson的分布式ID生成器，保证全局唯一
     *
     * @param key        业务key
     * @param expireTime 过期时间
     * @param initValue  ID初始值
     * @param stepValue  ID步长
     * @return 唯一id（long类型）
     */
    public static long getNextId(String key, Duration expireTime, long initValue, long stepValue) {
        // 获取ID生成器并生成下一个ID
        return getIdGenerator(key, expireTime, initValue, stepValue).nextId();
    }

    /**
     * 获取指定业务key的唯一id（简化参数）
     * 使用默认初始值和步长
     *
     * @param key        业务key
     * @param expireTime 过期时间
     * @return 唯一id
     */
    public static long getNextId(String key, Duration expireTime) {
        // 调用重载方法，使用默认初始值和步长
        return getIdGenerator(key, expireTime).nextId();
    }

    /**
     * 获取指定业务key的唯一id字符串（完整参数）
     * 将long类型ID转换为String类型
     *
     * @param key        业务key
     * @param expireTime 过期时间
     * @param initValue  ID初始值
     * @param stepValue  ID步长
     * @return 唯一id字符串
     */
    public static String getNextIdString(String key, Duration expireTime, long initValue, long stepValue) {
        // 获取ID并转换为字符串
        return Convert.toStr(getNextId(key, expireTime, initValue, stepValue));
    }

    /**
     * 获取指定业务key的唯一id字符串（简化参数）
     * 使用默认初始值和步长
     *
     * @param key        业务key
     * @param expireTime 过期时间
     * @return 唯一id
     */
    public static String getNextIdString(String key, Duration expireTime) {
        // 调用重载方法，使用默认初始值和步长
        return Convert.toStr(getNextId(key, expireTime));
    }

    /**
     * 获取指定业务key的唯一id字符串（带补位）
     * 不足指定位数时左补0，确保ID长度一致
     *
     * @param key        业务key
     * @param expireTime 过期时间
     * @param width      位数，不足左补0（如：width=6，ID=123 -> 000123）
     * @return 补零后的唯一id字符串
     */
    public static String getPaddedNextIdString(String key, Duration expireTime, Integer width) {
        // 获取ID字符串并左补0
        return StringUtils.leftPad(getNextIdString(key, expireTime), width, '0');
    }

    /**
     * 获取 yyyyMMdd 格式的唯一id
     * 格式：前缀 + 日期 + 递增ID（如：ORDER20250731000001）
     *
     * @return 唯一id
     * @deprecated 请使用 {@link #getDateId(String)} 或 {@link #getDateId(String, boolean)}、{@link #getDateId(String, boolean, int)}，确保不同业务的ID连续性
     */
    @Deprecated
    public static String getDateId() {
        // 调用重载方法，无业务前缀
        return getDateId("");
    }

    /**
     * 获取 prefix + yyyyMMdd 格式的唯一id
     * 每天重置，适用于按天生成的业务单号
     *
     * @param prefix 业务前缀（如：ORDER）
     * @return 唯一id（如：ORDER20250731）
     */
    public static String getDateId(String prefix) {
        // 默认携带业务前缀
        return getDateId(prefix, true);
    }

    /**
     * 获取 prefix + yyyyMMdd 格式的唯一id
     *
     * @param prefix       业务前缀
     * @param isWithPrefix id是否携带业务前缀
     * @return 唯一id
     */
    public static String getDateId(String prefix, boolean isWithPrefix) {
        // 不启用ID补位
        return getDateId(prefix, isWithPrefix, -1);
    }

    /**
     * 获取 prefix + yyyyMMdd 格式的唯一id（启用ID补位）
     * 补位长度 = {@link #DEFAULT_MIN_ID_CAPACITY_BITS}（6位）
     *
     * @param prefix       业务前缀
     * @param isWithPrefix id是否携带业务前缀
     * @return 唯一id（如：ORDER20250731000001）
     */
    public static String getPaddedDateId(String prefix, boolean isWithPrefix) {
        // 使用默认补位长度
        return getDateId(prefix, isWithPrefix, DEFAULT_MIN_ID_CAPACITY_BITS);
    }

    /**
     * 获取 prefix + yyyyMMdd 格式的唯一id（完整参数）
     * 每天重置，适用于按天生成的业务单号
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数，小于该位数的ID，左补0（小于等于0表示不启用补位）
     * @return 唯一id
     */
    public static String getDateId(String prefix, boolean isWithPrefix, int minIdCapacityBits) {
        // 使用当前日期
        return getDateId(prefix, isWithPrefix, minIdCapacityBits, LocalDate.now());
    }

    /**
     * 获取 prefix + yyyyMMdd 格式的唯一id（指定日期）
     * 用于生成历史日期的ID
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定日期
     * @return 唯一id
     */
    public static String getDateId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDate time) {
        // 使用默认初始值和步长
        return getDateId(prefix, isWithPrefix, minIdCapacityBits, time, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE);
    }

    /**
     * 获取 prefix + yyyyMMdd 格式的唯一id（完整参数）
     * 每天重置，适用于按天生成的业务单号
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定日期
     * @param initValue         ID初始值
     * @param stepValue         ID步长
     * @return 唯一id
     */
    public static String getDateId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDate time, long initValue, long stepValue) {
        // 调用通用方法，指定日期格式为yyyyMMdd
        return getDatePatternId(prefix, isWithPrefix, minIdCapacityBits, time, DatePattern.PURE_DATE_FORMATTER, DEFAULT_EXPIRE_TIME_DAY, initValue, stepValue);
    }

    /**
     * 获取 yyyyMMddHHmmss 格式的唯一id
     * 格式：前缀 + 日期时间 + 递增ID（如：ORDER202507311200000001）
     *
     * @return 唯一id
     * @deprecated 请使用 {@link #getDateTimeId(String)} 或 {@link #getDateTimeId(String, boolean)}、{@link #getDateTimeId(String, boolean, int)}，确保不同业务的ID连续性
     */
    @Deprecated
    public static String getDateTimeId() {
        // 调用重载方法，无业务前缀，不携带前缀
        return getDateTimeId("", false);
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 格式的唯一id
     * 每分钟重置，适用于按分钟生成的业务单号
     *
     * @param prefix 业务前缀（如：ORDER）
     * @return 唯一id（如：ORDER202507311200000001）
     */
    public static String getDateTimeId(String prefix) {
        // 默认携带业务前缀
        return getDateTimeId(prefix, true);
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 格式的唯一id
     *
     * @param prefix       业务前缀
     * @param isWithPrefix id是否携带业务前缀
     * @return 唯一id
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix) {
        // 不启用ID补位
        return getDateTimeId(prefix, isWithPrefix, -1);
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 格式的唯一id（启用ID补位）
     * 补位长度 = {@link #DEFAULT_MIN_ID_CAPACITY_BITS}（6位）
     *
     * @param prefix       业务前缀
     * @param isWithPrefix id是否携带业务前缀
     * @return 唯一id
     */
    public static String getPaddedDateTimeId(String prefix, boolean isWithPrefix) {
        // 使用默认补位长度
        return getDateTimeId(prefix, isWithPrefix, DEFAULT_MIN_ID_CAPACITY_BITS);
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 格式的唯一id（完整参数）
     * 每分钟重置，适用于按分钟生成的业务单号
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数，小于该位数的ID，左补0（小于等于0表示不启用补位）
     * @return 唯一id
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix, int minIdCapacityBits) {
        // 使用当前日期时间
        return getDateTimeId(prefix, isWithPrefix, minIdCapacityBits, LocalDateTime.now());
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 格式的唯一id（指定时间）
     * 用于生成历史时间的ID
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定日期时间
     * @return 唯一id
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDateTime time) {
        // 使用默认初始值和步长
        return getDateTimeId(prefix, isWithPrefix, minIdCapacityBits, time, DEFAULT_INIT_VALUE, DEFAULT_STEP_VALUE);
    }

    /**
     * 获取 prefix + yyyyMMddHHmmss 格式的唯一id（完整参数）
     * 每分钟重置，适用于按分钟生成的业务单号
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数
     * @param time              指定日期时间
     * @param initValue         ID初始值
     * @param stepValue         ID步长
     * @return 唯一id
     */
    public static String getDateTimeId(String prefix, boolean isWithPrefix, int minIdCapacityBits, LocalDateTime time, long initValue, long stepValue) {
        // 调用通用方法，指定日期时间格式为yyyyMMddHHmmss
        return getDatePatternId(prefix, isWithPrefix, minIdCapacityBits, time, DatePattern.PURE_DATETIME_FORMATTER, DEFAULT_EXPIRE_TIME_MINUTE, initValue, stepValue);
    }

    /**
     * 获取指定业务key的指定时间格式的ID（核心方法）
     * 通用ID生成方法，支持自定义时间格式、过期时间、初始值、步长
     *
     * @param prefix            业务前缀
     * @param isWithPrefix      id是否携带业务前缀
     * @param minIdCapacityBits 最小ID容量位数，小于该位数的ID，左补0（小于等于0表示不启用补位）
     * @param temporalAccessor  时间访问器（支持LocalDate、LocalDateTime等）
     * @param timeFormatter     时间格式（如：yyyyMMdd、yyyyMMddHHmmss）
     * @param expireTime        过期时间（到期后ID重置）
     * @param initValue         ID初始值
     * @param stepValue         ID步长
     * @return 唯一id
     */
    private static String getDatePatternId(String prefix, boolean isWithPrefix, int minIdCapacityBits, TemporalAccessor temporalAccessor, DateTimeFormatter timeFormatter, Duration expireTime, long initValue, long stepValue) {
        // 格式化时间前缀（如：20250731）
        String timePrefix = timeFormatter.format(temporalAccessor);
        // 业务前缀 + 时间前缀 构建 prefixKey（如：ORDER20250731）
        String prefixKey = StringUtils.format("{}{}", StringUtils.blankToDefault(prefix, ""), timePrefix);

        // 获取id，例 -> 1（基于prefixKey生成）
        String nextId = getNextIdString(prefixKey, expireTime, initValue, stepValue);

        // minIdCapacityBits 大于0，且 nextId 的长度小于 minIdCapacityBits，则左补0
        // 例如：nextId=1，minIdCapacityBits=6 -> 000001
        if (minIdCapacityBits > 0 && nextId.length() < minIdCapacityBits) {
            nextId = StringUtils.leftPad(nextId, minIdCapacityBits, '0');
        }

        // 是否携带业务前缀
        if (isWithPrefix) {
            // 例 -> ORDER20250731000001
            // 其中 ORDER 为业务前缀，20250731 为 yyyyMMdd 格式时间, 000001 为nextId
            return StringUtils.format("{}{}", prefixKey, nextId);
        }
        // 例 -> 20250731000001
        // 其中 20250731 为 yyyyMMdd 格式时间, 000001 为nextId
        return StringUtils.format("{}{}", timePrefix, nextId);
    }
}

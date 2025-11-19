// 时间工具类，继承Apache Commons Lang3的DateUtils
// 提供日期格式化、解析、计算、转换等常用时间操作方法
package org.dromara.common.core.utils;

// Apache Commons Lang3日期格式化工具类
import org.apache.commons.lang3.time.DateFormatUtils;
// 日期格式枚举（如YYYY_MM_DD、YYYYMMDDHHMMSS等预定义格式）
import org.dromara.common.core.enums.FormatsType;
// 业务异常类（用于日期校验失败时抛出）
import org.dromara.common.core.exception.ServiceException;

// Java管理工厂，用于获取JVM运行时信息（如启动时间）
import java.lang.management.ManagementFactory;
// 日期解析异常
import java.text.ParseException;
// 日期格式化类（线程不安全，每次使用需创建新实例）
import java.text.SimpleDateFormat;
// Java 8时间API（LocalDateTime、LocalDate、ZonedDateTime等）
import java.time.*;
// 传统日期类（与Java 8时间API互转）
import java.util.Date;
// 时间单位枚举（DAYS、HOURS、MINUTES、SECONDS等）
import java.util.concurrent.TimeUnit;

/**
 * 时间工具类
 * 继承Apache Commons Lang3的DateUtils，提供增强的日期时间操作能力
 * 包括日期格式化、解析、计算、转换、校验等功能
 * 支持传统Date与Java 8时间API（LocalDateTime/LocalDate）互转
 *
 * @author ruoyi
 */
// 继承Apache Commons Lang3的DateUtils，复用其日期解析能力
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    
    /**
     * 预定义的日期解析模式数组
     * 支持多种常见日期格式自动识别，用于parseDate方法
     * 包括：yyyy-MM-dd、yyyy-MM-dd HH:mm:ss、yyyy/MM/dd等多种分隔符格式
     */
    private static final String[] PARSE_PATTERNS = {
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
        "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
        "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 私有构造方法（已废弃）
     * @deprecated 工具类不应被实例化，所有方法均为静态方法
     */
    @Deprecated
    private DateUtils() {
        // 工具类私有化构造方法，防止被实例化
    }

    /**
     * 获取当前日期和时间
     * 返回当前系统时间的Date对象
     *
     * @return 当前日期和时间的Date对象表示
     */
    public static Date getNowDate() {
        // 创建新的Date对象（Date构造函数使用当前系统时间）
        return new Date();
    }

    /**
     * 获取当前日期的字符串表示，格式为YYYY-MM-DD
     * 使用FormatsType.YYYY_MM_DD格式
     *
     * @return 当前日期的字符串表示（如：2025-11-18）
     */
    public static String getDate() {
        // 调用dateTimeNow方法，指定格式为YYYY_MM_DD
        return dateTimeNow(FormatsType.YYYY_MM_DD);
    }

    /**
     * 获取当前日期的字符串表示，格式为yyyyMMdd
     * 无分隔符的紧凑格式，常用于文件名、日志等场景
     *
     * @return 当前日期的字符串表示（如：20251118）
     */
    public static String getCurrentDate() {
        // 使用DateFormatUtils.format格式化当前日期，格式为YYYYMMDD
        return DateFormatUtils.format(new Date(), FormatsType.YYYYMMDD.getTimeFormat());
    }

    /**
     * 获取当前日期的路径格式字符串，格式为"yyyy/MM/dd"
     * 使用斜杠分隔，常用于按日期分层的文件路径（如：/2025/11/18/）
     *
     * @return 当前日期的路径格式字符串（如：2025/11/18）
     */
    public static String datePath() {
        // 获取当前日期对象
        Date now = new Date();
        // 使用DateFormatUtils.format格式化，格式为YYYY_MM_DD_SLASH（yyyy/MM/dd）
        return DateFormatUtils.format(now, FormatsType.YYYY_MM_DD_SLASH.getTimeFormat());
    }

    /**
     * 获取当前时间的字符串表示，格式为YYYY-MM-DD HH:MM:SS
     * 标准日期时间格式，包含年月日时分秒
     *
     * @return 当前时间的字符串表示（如：2025-11-18 09:20:30）
     */
    public static String getTime() {
        // 调用dateTimeNow方法，指定格式为YYYY_MM_DD_HH_MM_SS
        return dateTimeNow(FormatsType.YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前时间的字符串表示，格式为"HH:MM:SS"
     * 仅包含时分秒，不包含日期
     *
     * @return 当前时间的字符串表示，格式为"HH:MM:SS"（如：09:20:30）
     */
    public static String getTimeWithHourMinuteSecond() {
        // 调用dateTimeNow方法，指定格式为HH_MM_SS
        return dateTimeNow(FormatsType.HH_MM_SS);
    }

    /**
     * 获取当前日期和时间的字符串表示，格式为YYYYMMDDHHMMSS
     * 无分隔符的紧凑格式，常用于生成唯一标识、时间戳等
     *
     * @return 当前日期和时间的字符串表示（如：20251118092030）
     */
    public static String dateTimeNow() {
        // 调用dateTimeNow方法，指定格式为YYYYMMDDHHMMSS
        return dateTimeNow(FormatsType.YYYYMMDDHHMMSS);
    }

    /**
     * 获取当前日期和时间的指定格式的字符串表示
     * 支持自定义格式，通过FormatsType枚举指定
     *
     * @param format 日期时间格式枚举，如FormatsType.YYYY_MM_DD_HH_MM_SS
     * @return 当前日期和时间的字符串表示
     */
    public static String dateTimeNow(final FormatsType format) {
        // 调用parseDateToStr方法，传入格式和当前日期对象
        return parseDateToStr(format, new Date());
    }

    /**
     * 将指定日期格式化为YYYY-MM-DD格式的字符串
     * 常用于日期显示、数据导出等场景
     *
     * @param date 要格式化的日期对象
     * @return 格式化后的日期字符串（如：2025-11-18）
     */
    public static String formatDate(final Date date) {
        // 调用parseDateToStr方法，指定格式为YYYY_MM_DD
        return parseDateToStr(FormatsType.YYYY_MM_DD, date);
    }

    /**
     * 将指定日期格式化为YYYY-MM-DD HH:MM:SS格式的字符串
     * 标准日期时间格式，常用于日志、数据展示等
     *
     * @param date 要格式化的日期对象
     * @return 格式化后的日期时间字符串（如：2025-11-18 09:20:30）
     */
    public static String formatDateTime(final Date date) {
        // 调用parseDateToStr方法，指定格式为YYYY_MM_DD_HH_MM_SS
        return parseDateToStr(FormatsType.YYYY_MM_DD_HH_MM_SS, date);
    }

    /**
     * 将指定日期按照指定格式进行格式化
     * 核心格式化方法，使用SimpleDateFormat进行格式化
     * 注意：SimpleDateFormat线程不安全，每次调用都创建新实例
     *
     * @param format 日期时间格式枚举，如FormatsType.YYYY_MM_DD_HH_MM_SS
     * @param date   要格式化的日期对象
     * @return 格式化后的日期时间字符串
     */
    public static String parseDateToStr(final FormatsType format, final Date date) {
        // 创建SimpleDateFormat对象，指定格式（线程不安全，必须每次创建新实例）
        // 调用format方法格式化日期对象为字符串
        return new SimpleDateFormat(format.getTimeFormat()).format(date);
    }

    /**
     * 将指定格式的日期时间字符串转换为Date对象
     * 与parseDateToStr相反，将字符串解析为Date对象
     *
     * @param format 要解析的日期时间格式枚举，如FormatsType.YYYY_MM_DD_HH_MM_SS
     * @param ts     要解析的日期时间字符串
     * @return 解析后的Date对象
     * @throws RuntimeException 如果解析过程中发生ParseException异常
     */
    public static Date parseDateTime(final FormatsType format, final String ts) {
        try {
            // 创建SimpleDateFormat对象，指定格式
            // 调用parse方法将字符串解析为Date对象
            return new SimpleDateFormat(format.getTimeFormat()).parse(ts);
        } catch (ParseException e) {
            // 解析失败时抛出RuntimeException，包装原始异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象转换为日期对象
     * 智能解析方法，尝试使用预定义的多种格式进行解析
     * 支持12种常见日期格式（见PARSE_PATTERNS数组）
     *
     * @param str 要转换的对象，通常是字符串
     * @return 转换后的日期对象，如果转换失败或输入为null，则返回null（不抛出异常）
     */
    public static Date parseDate(Object str) {
        // 如果输入对象为null，直接返回null
        if (str == null) {
            return null;
        }
        try {
            // 调用父类parseDate方法，尝试使用预定义的格式数组解析
            // parseDate会遍历PARSE_PATTERNS，尝试每种格式直到成功
            return parseDate(str.toString(), PARSE_PATTERNS);
        } catch (ParseException e) {
            // 所有格式都解析失败时，返回null（不抛出异常，提升健壮性）
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     * 通过JMX获取JVM启动时间，即应用启动时间
     *
     * @return 服务器启动时间的Date对象表示
     */
    public static Date getServerStartDate() {
        // 通过ManagementFactory获取RuntimeMXBean（运行时管理Bean）
        // 调用getStartTime()获取JVM启动时间（毫秒时间戳）
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        // 将时间戳转换为Date对象
        return new Date(time);
    }

    /**
     * 计算两个时间之间的时间差，并以指定单位返回（绝对值）
     * 支持天、小时、分钟、秒、毫秒、微秒、纳秒等多种单位
     *
     * @param start 起始时间
     * @param end   结束时间
     * @param unit  所需返回的时间单位（DAYS、HOURS、MINUTES、SECONDS、MILLISECONDS、MICROSECONDS、NANOSECONDS）
     * @return 时间差的绝对值（取绝对值避免负数），以指定单位表示
     */
    public static long difference(Date start, Date end, TimeUnit unit) {
        // 计算时间差，单位为毫秒（end.getTime() - start.getTime()）
        // Math.abs取绝对值，确保结果为正数（避免时间顺序影响）
        long diffInMillis = Math.abs(end.getTime() - start.getTime());

        // 使用Java 14+ switch表达式根据目标单位转换时间差
        return switch (unit) {
            // 转换为天数（除以一天的毫秒数）
            case DAYS -> diffInMillis / TimeUnit.DAYS.toMillis(1);
            // 转换为小时数（除以一小时的毫秒数）
            case HOURS -> diffInMillis / TimeUnit.HOURS.toMillis(1);
            // 转换为分钟数（除以一分钟的毫秒数）
            case MINUTES -> diffInMillis / TimeUnit.MINUTES.toMillis(1);
            // 转换为秒数（除以一秒的毫秒数）
            case SECONDS -> diffInMillis / TimeUnit.SECONDS.toMillis(1);
            // 保持毫秒单位，直接返回
            case MILLISECONDS -> diffInMillis;
            // 转换为微秒（毫秒转微秒）
            case MICROSECONDS -> TimeUnit.MILLISECONDS.toMicros(diffInMillis);
            // 转换为纳秒（毫秒转纳秒）
            case NANOSECONDS -> TimeUnit.MILLISECONDS.toNanos(diffInMillis);
        };
    }

    /**
     * 计算两个日期之间的时间差，并以天、小时和分钟的格式返回
     * 格式固定为"X天 Y小时 Z分钟"，不省略0值单位
     *
     * @param endDate 结束日期
     * @param nowDate 当前日期
     * @return 表示时间差的字符串，格式为"X天 Y小时 Z分钟"
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        // 计算时间差（毫秒数）
        long diffInMillis = endDate.getTime() - nowDate.getTime();
        // 计算天数（总毫秒数转天数）
        long day = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        // 计算小时数（总毫秒数转小时数，对24取模得到剩余小时）
        long hour = TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24;
        // 计算分钟数（总毫秒数转分钟数，对60取模得到剩余分钟）
        long min = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;
        // 使用String.format格式化输出，格式为"X天 Y小时 Z分钟"
        return String.format("%d天 %d小时 %d分钟", day, hour, min);
    }

    /**
     * 计算两个时间点的差值（天、小时、分钟、秒），当值为0时不显示该单位
     * 智能格式化，省略0值单位，使结果更简洁
     *
     * @param endDate 结束时间
     * @param nowDate 当前时间
     * @return 时间差字符串，格式为 "X天 Y小时 Z分钟 W秒"，若为0则不显示该单位
     */
    public static String getTimeDifference(Date endDate, Date nowDate) {
        // 计算时间差（毫秒数）
        long diffInMillis = endDate.getTime() - nowDate.getTime();
        // 计算天数
        long day = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        // 计算小时数（对24取模）
        long hour = TimeUnit.MILLISECONDS.toHours(diffInMillis) % 24;
        // 计算分钟数（对60取模）
        long min = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) % 60;
        // 计算秒数（对60取模）
        long sec = TimeUnit.MILLISECONDS.toSeconds(diffInMillis) % 60;
        // 构建时间差字符串，条件是值不为0才显示该单位
        StringBuilder result = new StringBuilder();
        // 如果天数大于0，追加"X天 "
        if (day > 0) {
            result.append(String.format("%d天 ", day));
        }
        // 如果小时数大于0，追加"Y小时 "
        if (hour > 0) {
            result.append(String.format("%d小时 ", hour));
        }
        // 如果分钟数大于0，追加"Z分钟 "
        if (min > 0) {
            result.append(String.format("%d分钟 ", min));
        }
        // 如果秒数大于0，追加"W秒"
        if (sec > 0) {
            result.append(String.format("%d秒", sec));
        }
        // 如果result长度大于0，返回trim后的字符串；否则返回"0秒"
        return result.length() > 0 ? result.toString().trim() : "0秒";
    }

    /**
     * 将LocalDateTime对象转换为Date对象
     * Java 8时间API与传统Date的互转
     *
     * @param temporalAccessor 要转换的LocalDateTime对象
     * @return 转换后的Date对象
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        // 将LocalDateTime转换为ZonedDateTime（带时区信息）
        // 使用系统默认时区（ZoneId.systemDefault()）
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        // 将ZonedDateTime转换为Instant（时间戳），再转换为Date
        return Date.from(zdt.toInstant());
    }

    /**
     * 将LocalDate对象转换为Date对象
     * LocalDate只有日期部分，时间部分设置为00:00:00
     *
     * @param temporalAccessor 要转换的LocalDate对象
     * @return 转换后的Date对象（时间部分为00:00:00）
     */
    public static Date toDate(LocalDate temporalAccessor) {
        // 将LocalDate和LocalTime组合为LocalDateTime（时间设置为00:00:00）
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        // 转换为ZonedDateTime（带系统默认时区）
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        // 转换为Date对象
        return Date.from(zdt.toInstant());
    }

    /**
     * 校验日期范围
     * 用于业务逻辑中的日期合法性检查，如查询条件、表单提交等
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param maxValue  最大时间跨度的限制值
     * @param unit      时间跨度的单位，可选择DAYS、HOURS或MINUTES
     * @throws ServiceException 如果校验失败（结束日期早于开始日期，或时间跨度超过限制）
     */
    public static void validateDateRange(Date startDate, Date endDate, int maxValue, TimeUnit unit) {
        // 校验结束日期不能早于开始日期（业务逻辑要求）
        if (endDate.before(startDate)) {
            // 抛出业务异常，提示"结束日期不能早于开始日期"
            throw new ServiceException("结束日期不能早于开始日期");
        }

        // 计算时间跨度（毫秒数）
        long diffInMillis = endDate.getTime() - startDate.getTime();

        // 使用switch表达式根据单位转换时间跨度
        long diff = switch (unit) {
            // 转换为天数
            case DAYS -> TimeUnit.MILLISECONDS.toDays(diffInMillis);
            // 转换为小时数
            case HOURS -> TimeUnit.MILLISECONDS.toHours(diffInMillis);
            // 转换为分钟数
            case MINUTES -> TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            // 其他单位抛出非法参数异常
            default -> throw new IllegalArgumentException("不支持的时间单位");
        };

        // 校验时间跨度不超过最大限制（防止查询范围过大导致性能问题）
        if (diff > maxValue) {
            // 抛出业务异常，提示"最大时间跨度为 X 单位"
            throw new ServiceException("最大时间跨度为 {} {}", maxValue, unit.toString().toLowerCase());
        }
    }

}

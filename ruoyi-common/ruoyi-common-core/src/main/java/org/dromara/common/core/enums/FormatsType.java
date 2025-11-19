package org.dromara.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.common.core.utils.StringUtils;

/*
 * 日期格式说明（基于Java SimpleDateFormat）
 * "yyyy"：4位数的年份，例如：2023年表示为"2023"。
 * "yy"：2位数的年份，例如：2023年表示为"23"。
 * "MM"：2位数的月份，取值范围为01到12，例如：7月表示为"07"。
 * "M"：不带前导零的月份，取值范围为1到12，例如：7月表示为"7"。
 * "dd"：2位数的日期，取值范围为01到31，例如：22日表示为"22"。
 * "d"：不带前导零的日期，取值范围为1到31，例如：22日表示为"22"。
 * "EEEE"：星期的全名，例如：星期三表示为"Wednesday"。
 * "E"：星期的缩写，例如：星期三表示为"Wed"。
 * "DDD" 或 "D"：一年中的第几天，取值范围为001到366，例如：第200天表示为"200"。
 * 时间格式说明
 * "HH"：24小时制的小时数，取值范围为00到23，例如：下午5点表示为"17"。
 * "hh"：12小时制的小时数，取值范围为01到12，例如：下午5点表示为"05"。
 * "mm"：分钟数，取值范围为00到59，例如：30分钟表示为"30"。
 * "ss"：秒数，取值范围为00到59，例如：45秒表示为"45"。
 * "SSS"：毫秒数，取值范围为000到999，例如：123毫秒表示为"123"。
 */

/**
 * 日期格式与时间格式枚举
 * 提供系统中常用的日期时间格式模板，统一时间格式化标准
 * 支持多种分隔符：-、/、. 和无分隔符
 */
// Lombok注解：自动生成getter方法
@Getter
// Lombok注解：生成全参构造函数
@AllArgsConstructor
public enum FormatsType {

    /**
     * 2位年份格式
     * 例如：2023年表示为"23"
     */
    YY("yy"),

    /**
     * 4位年份格式
     * 例如：2023年表示为"2023"
     */
    YYYY("yyyy"),

    /**
     * 年月格式（横线分隔）
     * 例如，2023年7月可以表示为 "2023-07"
     */
    YYYY_MM("yyyy-MM"),

    /**
     * 年月日格式（横线分隔）
     * 例如，日期 "2023年7月22日" 可以表示为 "2023-07-22"
     */
    YYYY_MM_DD("yyyy-MM-dd"),

    /**
     * 年月日时分格式（横线分隔）
     * 例如，当前时间如果是 "2023年7月22日下午3点30分"，则可以表示为 "2023-07-22 15:30"
     */
    YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm"),

    /**
     * 年月日时分秒格式（横线分隔）
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023-07-22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),

    /**
     * 时分秒格式
     * 例如：下午3点30分45秒，表示为 "15:30:45"
     */
    HH_MM_SS("HH:mm:ss"),

    /**
     * 年月格式（斜杠分隔）
     * 例如，2023年7月可以表示为 "2023/07"
     */
    YYYY_MM_SLASH("yyyy/MM"),

    /**
     * 年月日格式（斜杠分隔）
     * 例如，日期 "2023年7月22日" 可以表示为 "2023/07/22"
     */
    YYYY_MM_DD_SLASH("yyyy/MM/dd"),

    /**
     * 年月日时分格式（斜杠分隔）
     * 例如，当前时间如果是 "2023年7月22日下午3点30分"，则可以表示为 "2023/07/22 15:30"
     */
    YYYY_MM_DD_HH_MM_SLASH("yyyy/MM/dd HH:mm"),

    /**
     * 年月日时分秒格式（斜杠分隔）
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023/07/22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS_SLASH("yyyy/MM/dd HH:mm:ss"),

    /**
     * 年月格式（点分隔）
     * 例如，2023年7月可以表示为 "2023.07"
     */
    YYYY_MM_DOT("yyyy.MM"),

    /**
     * 年月日格式（点分隔）
     * 例如，日期 "2023年7月22日" 可以表示为 "2023.07.22"
     */
    YYYY_MM_DD_DOT("yyyy.MM.dd"),

    /**
     * 年月日时分格式（点分隔）
     * 例如，当前时间如果是 "2023年7月22日下午3点30分"，则可以表示为 "2023.07.22 15:30"
     */
    YYYY_MM_DD_HH_MM_DOT("yyyy.MM.dd HH:mm"),

    /**
     * 年月日时分秒格式（点分隔）
     * 例如，当前时间如果是 "2023年7月22日下午3点30分45秒"，则可以表示为 "2023.07.22 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS_DOT("yyyy.MM.dd HH:mm:ss"),

    /**
     * 年月格式（无分隔符）
     * 例如，2023年7月可以表示为 "202307"
     */
    YYYYMM("yyyyMM"),

    /**
     * 年月日格式（无分隔符）
     * 例如，2023年7月22日可以表示为 "20230722"
     */
    YYYYMMDD("yyyyMMdd"),

    /**
     * 年月日时格式（无分隔符）
     * 例如，2023年7月22日下午3点可以表示为 "2023072215"
     */
    YYYYMMDDHH("yyyyMMddHH"),

    /**
     * 年月日时分格式（无分隔符）
     * 例如，2023年7月22日下午3点30分可以表示为 "202307221530"
     */
    YYYYMMDDHHMM("yyyyMMddHHmm"),

    /**
     * 年月日时分秒格式（无分隔符）
     * 例如，2023年7月22日下午3点30分45秒可以表示为 "20230722153045"
     */
    YYYYMMDDHHMMSS("yyyyMMddHHmmss");

    /**
     * 时间格式字符串
     * 存储对应的SimpleDateFormat格式模板
     */
    private final String timeFormat;

    /**
     * 根据字符串查找对应的格式枚举
     * 遍历所有枚举值，查找包含指定格式字符串的枚举
     *
     * @param str 要查找的格式字符串
     * @return 对应的FormatsType枚举
     * @throws RuntimeException 如果找不到匹配的枚举
     */
    public static FormatsType getFormatsType(String str) {
        // 遍历所有枚举值
        for (FormatsType value : values()) {
            // 判断字符串是否包含该格式的特征
            if (StringUtils.contains(str, value.getTimeFormat())) {
                return value;
            }
        }
        // 如果找不到匹配的枚举，抛出运行时异常
        throw new RuntimeException("'FormatsType' not found By " + str);
    }
}

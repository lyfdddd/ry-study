// IP地址解析工具类，用于根据IP地址查询归属地信息
package org.dromara.common.core.utils.ip;

// Hutool HTML工具类，用于过滤HTML标签防止XSS攻击
import cn.hutool.http.HtmlUtil;
// Lombok注解：设置构造方法访问级别为私有，防止类被实例化
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 网络工具类（判断IP类型、内外网等）
import org.dromara.common.core.utils.NetUtils;
// 字符串工具类（处理空字符串默认值）
import org.dromara.common.core.utils.StringUtils;

/**
 * IP地址解析工具类
 * 根据IP地址查询归属地信息（国家、省份、城市）
 * 支持IPv4和IPv6地址，内网IP直接返回"内网IP"
 * 使用ip2region库实现IP地址定位
 *
 * @author Lion Li
 */
// Lombok注解：自动生成slf4j日志对象，提供日志记录能力
@Slf4j
// Lombok注解：生成私有构造方法，防止工具类被实例化
// access = AccessLevel.PRIVATE确保构造方法私有
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    /**
     * 未知IP地址的默认返回值
     * 当IP格式非法或无法解析时返回"XX XX"
     */
    public static final String UNKNOWN_IP = "XX XX";
    
    /**
     * 内网IP地址的返回值
     * 当IP为内网地址（如192.168.x.x、10.x.x.x等）时返回"内网IP"
     */
    public static final String LOCAL_ADDRESS = "内网IP";
    
    /**
     * 未知地址的返回值
     * 当IP为IPv6且不支持解析时返回"未知"
     */
    public static final String UNKNOWN_ADDRESS = "未知";

    /**
     * 根据IP地址查询真实地理位置
     * 主入口方法，支持IPv4和IPv6地址
     *
     * @param ip IP地址字符串（如"192.168.1.1"、"2001:0db8:85a3:0000:0000:8a2e:0370:7334"）
     * @return IP归属地信息（如"中国 北京 北京市"、"内网IP"、"未知"等）
     */
    public static String getRealAddressByIP(String ip) {
        // 处理空字符串并过滤HTML标签，防止XSS攻击
        // StringUtils.blankToDefault将null或空字符串转换为""，避免NPE
        // HtmlUtil.cleanHtmlTag过滤用户输入中的HTML标签，防止恶意脚本注入
        ip = HtmlUtil.cleanHtmlTag(StringUtils.blankToDefault(ip,""));
        
        // 判断是否为IPv4地址（使用NetUtils工具类）
        // IPv4格式：xxx.xxx.xxx.xxx，每段0-255
        if (NetUtils.isIPv4(ip)) {
            // 调用IPv4解析方法，查询归属地
            return resolverIPv4Region(ip);
        }
        
        // 判断是否为IPv6地址（使用NetUtils工具类）
        // IPv6格式：8组16进制数，用冒号分隔
        if (NetUtils.isIPv6(ip)) {
            // 调用IPv6解析方法，查询归属地
            return resolverIPv6Region(ip);
        }
        
        // 如果不是IPv4也不是IPv6，则返回未知IP标识
        // 可能是格式非法的IP字符串
        return UNKNOWN_IP;
    }

    /**
     * 根据IPv4地址查询IP归属行政区域
     * 使用ip2region库查询IP地址库，获取国家、省份、城市信息
     * 内网IP直接返回"内网IP"，不查询IP库（节省性能）
     *
     * @param ip IPv4地址（如"223.104.3.1"）
     * @return 归属行政区域（如"中国 广东 深圳"、"内网IP"）
     */
    private static String resolverIPv4Region(String ip){
        // 判断是否为内网IP（如192.168.x.x、10.x.x.x、172.16.x.x等）
        // 内网IP不需要查询IP库，直接返回"内网IP"
        if (NetUtils.isInnerIP(ip)) {
            return LOCAL_ADDRESS;
        }
        // 调用RegionUtils查询IP归属地
        // RegionUtils封装了ip2region库，查询本地IP数据库文件（ip2region.xdb）
        return RegionUtils.getCityInfo(ip);
    }

    /**
     * 根据IPv6地址查询IP归属行政区域
     * 当前ip2region库不支持IPv6地址解析，直接返回"未知"
     * 内网IPv6地址返回"内网IP"
     *
     * @param ip IPv6地址（如"2001:0db8:85a3:0000:0000:8a2e:0370:7334"）
     * @return 归属行政区域（当前固定返回"未知"或"内网IP"）
     */
    private static String resolverIPv6Region(String ip){
        // 判断是否为内网IPv6地址（如fe80::、::1等）
        // 内网IPv6不需要查询，直接返回"内网IP"
        if (NetUtils.isInnerIPv6(ip)) {
            return LOCAL_ADDRESS;
        }
        // 记录警告日志，提示ip2region不支持IPv6解析
        // 如有需要，可自行实现IPv6地址信息解析逻辑（如调用在线API）
        log.warn("ip2region不支持IPV6地址解析：{}", ip);
        // 不支持IPv6，不再进行没有必要的IP地址信息的解析，直接返回"未知"
        return UNKNOWN_ADDRESS;
    }

}

// 网络工具类，提供IP地址相关判断功能
package org.dromara.common.core.utils;

// Hutool预定义正则表达式模式池（包含IPv4、IPv6、邮箱、手机号等常用正则）
import cn.hutool.core.lang.PatternPool;
// Hutool网络工具类（提供IP、端口等基础网络操作）
import cn.hutool.core.net.NetUtil;
// Lombok注解：设置构造方法访问级别为私有，防止类被实例化
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 正则表达式工具类（封装正则匹配逻辑）
import org.dromara.common.core.utils.regex.RegexUtils;

// Java网络编程核心类
import java.net.Inet6Address; // IPv6地址类
import java.net.InetAddress; // IP地址基类
import java.net.UnknownHostException; // 未知主机异常

/**
 * 增强网络相关工具类
 * 继承Hutool的NetUtil，提供IP地址类型判断、内外网判断等增强功能
 * 支持IPv4和IPv6地址的格式验证和分类
 *
 * @author 秋辞未寒
 */
// Lombok注解：自动生成slf4j日志对象，提供日志记录能力
@Slf4j
// Lombok注解：生成私有构造方法，防止工具类被实例化
// access = AccessLevel.PRIVATE确保构造方法私有，工具类无需实例化
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NetUtils extends NetUtil {

    /**
     * 判断是否为IPv6地址
     * 通过InetAddress.getByName解析IP字符串，判断实例类型是否为Inet6Address
     * 支持标准IPv6格式（如2001:0db8:85a3:0000:0000:8a2e:0370:7334）和压缩格式
     *
     * @param ip IP地址字符串
     * @return true表示是IPv6地址，false表示不是IPv6地址或格式错误
     */
    public static boolean isIPv6(String ip) {
        try {
            // 使用InetAddress.getByName解析IP字符串
            // 如果解析成功且返回Inet6Address实例，则说明是IPv6地址
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            // 解析失败（格式错误），返回false
            return false;
        }
    }

    /**
     * 判断IPv6地址是否为内网地址
     * IPv6内网地址包括：链路本地地址、唯一本地地址、环回地址等
     * <br><br>
     * 以下地址将归类为本地地址，如有业务场景有需要，请根据需求自行处理：
     * <pre>
     * 通配符地址 0:0:0:0:0:0:0:0（::）
     * 链路本地地址 fe80::/10（用于局域网通信）
     * 唯一本地地址 fec0::/10（类似IPv4的私有地址）
     * 环回地址 ::1（类似IPv4的127.0.0.1，表示本机）
     * </pre>
     *
     * @param ip IP地址字符串
     * @return true表示是内网地址，false表示是公网地址
     * @throws IllegalArgumentException 如果IP地址格式无效，抛出非法参数异常
     */
    public static boolean isInnerIPv6(String ip) {
        try {
            // 判断是否为IPv6地址
            if (InetAddress.getByName(ip) instanceof Inet6Address inet6Address) {
                // 使用Java 14+ instanceof模式匹配，直接转换为Inet6Address
                // isAnyLocalAddress() 判断是否为通配符地址（::），通常不会将其视为内网地址，根据业务场景自行处理判断
                // isLinkLocalAddress() 判断是否为链路本地地址（fe80::/10），通常不算内网地址，是否划分归属于内网需要根据业务场景自行处理判断
                // isLoopbackAddress() 判断是否为环回地址（::1），与IPv4的127.0.0.1同理，用于表示本机
                // isSiteLocalAddress() 判断是否为本地站点地址，IPv6唯一本地地址（Unique Local Addresses，简称ULA）
                if (inet6Address.isAnyLocalAddress()
                    || inet6Address.isLinkLocalAddress()
                    || inet6Address.isLoopbackAddress()
                    || inet6Address.isSiteLocalAddress()) {
                    // 满足任一条件即认为是内网地址
                    return true;
                }
            }
        } catch (UnknownHostException e) {
            // 注意：isInnerIPv6方法和isIPv6方法的适用范围不同
            // isIPv6返回false即可，但isInnerIPv6需要明确知道是否为内网，所以此处不能忽略异常信息
            // 抛出IllegalArgumentException，附带原始异常信息，便于排查问题
            throw new IllegalArgumentException("Invalid IPv6 address!", e);
        }
        // 不是内网地址，返回false
        return false;
    }

    /**
     * 判断是否为IPv4地址
     * 使用正则表达式验证IPv4格式：xxx.xxx.xxx.xxx（每段0-255）
     * 通过PatternPool.IPV4获取预编译的正则表达式，提升性能
     *
     * @param ip IP地址字符串
     * @return true表示是IPv4地址，false表示不是IPv4地址
     */
    public static boolean isIPv4(String ip) {
        // 调用RegexUtils.isMatch方法，使用预编译的IPv4正则表达式进行匹配
        // PatternPool.IPV4是Hutool提供的预定义正则模式，避免重复编译提升性能
        return RegexUtils.isMatch(PatternPool.IPV4, ip);
    }

}

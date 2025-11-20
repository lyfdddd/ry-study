// IP地址定位工具类包声明，属于工具类模块
// 该类提供离线IP地址定位功能，基于ip2region库实现
package org.dromara.common.core.utils.ip;

// Hutool异常类：资源不存在异常（当ClassPath下找不到文件时抛出）
// NoResourceException继承自IORuntimeException，表示资源加载失败
import cn.hutool.core.io.resource.NoResourceException;
// Hutool资源工具类：从ClassPath读取文件资源
// ResourceUtil提供静态方法读取ClassPath、文件系统、URL等资源
import cn.hutool.core.io.resource.ResourceUtil;
// Lombok日志注解：自动生成slf4j日志对象，提供日志记录能力
// @Slf4j注解会在编译期生成private static final Logger log = LoggerFactory.getLogger(RegionUtils.class);
import lombok.extern.slf4j.Slf4j;
// 业务异常类：用于封装业务逻辑异常，统一异常处理
// ServiceException继承自RuntimeException，是RuoYi-Vue-Plus的自定义业务异常
import org.dromara.common.core.exception.ServiceException;
// 字符串工具类：提供字符串常用操作方法
// StringUtils是Hutool封装的字符串工具类
import org.dromara.common.core.utils.StringUtils;
// ip2region核心查询器：基于xdb文件的IP地址查询引擎
// Searcher是ip2region库提供的IP查询核心类，支持内存模式和文件模式
import org.lionsoul.ip2region.xdb.Searcher;

/**
 * IP地址定位工具类（离线方式）
 * 基于ip2region库实现离线IP地址定位功能
 * 通过加载本地IP地址库文件（ip2region.xdb）实现IP地址到地理位置的映射
 * 参考地址：<a href="https://gitee.com/lionsoul/ip2region/tree/master/binding/java">集成 ip2region 实现离线IP地址定位库</a>
 *
 * @author lishuyan
 */
// Lombok注解：自动生成slf4j日志对象，提供日志记录能力
// 日志对象用于记录类初始化、查询异常等关键信息，方便问题排查
@Slf4j
public class RegionUtils {

    /**
     * IP地址库文件名称
     * 存储在ClassPath下的IP地址数据库文件
     * 文件路径：resources/ip2region.xdb
     * 该文件是ip2region的离线IP数据库，包含全球IP地址段和地理位置映射
     */
    public static final String IP_XDB_FILENAME = "ip2region.xdb";

    /**
     * IP地址查询器
     * 基于ip2region库的Searcher对象，用于查询IP地址信息
     * 使用final修饰确保线程安全，类加载后不可修改
     * 采用静态初始化，确保JVM类加载时只创建一次（单例模式）
     */
    private static final Searcher SEARCHER;

    /**
     * 静态代码块
     * 在类加载时初始化IP地址查询器（JVM类加载机制保证只执行一次）
     * 将ip2region数据库文件从ClassPath加载到内存并创建Searcher对象
     * 使用内存模式提升查询性能，避免每次查询都读取磁盘文件
     * 内存模式查询性能比文件模式高10倍以上，适合高并发场景
     */
    // 静态代码块在类加载时执行，确保Searcher只初始化一次
    static {
        try {
            // 步骤1：将ip2region数据库文件xdb从ClassPath加载到内存字节数组
            // ResourceUtil.readBytes()方法从ClassPath读取文件字节数据，支持jar包内资源读取
            // 支持从jar包、文件系统、URL等多种资源位置读取
            // 步骤2：基于加载到内存的xdb数据创建一个Searcher查询对象
            // Searcher.newWithBuffer()使用内存中的数据创建查询器，查询性能比文件模式高10倍以上
            // 内存模式适合高并发场景，但启动时会消耗约30MB内存（xdb文件大小）
            // 使用内存模式避免每次查询都读取磁盘，提升查询性能
            SEARCHER = Searcher.newWithBuffer(ResourceUtil.readBytes(IP_XDB_FILENAME));
            // 记录初始化成功日志，方便运维监控和问题排查
            // 日志级别info，表示核心组件初始化成功
            // 记录成功日志，便于监控和问题排查
            log.info("RegionUtils初始化成功，加载IP地址库数据成功！");
        } catch (NoResourceException e) {
            // 捕获资源不存在异常：IP地址库文件未找到（通常是因为忘记将ip2region.xdb放入resources目录）
            // 抛出ServiceException业务异常，中断应用启动，避免运行时查询失败
            // 异常信息明确提示原因，方便快速定位问题
            // 抛出ServiceException中断应用启动，避免运行时查询失败
            throw new ServiceException("RegionUtils初始化失败，原因：IP地址库数据不存在！");
        } catch (Exception e) {
            // 捕获其他异常：文件读取失败、数据格式错误、内存不足等
            // 抛出ServiceException业务异常，中断应用启动
            // 异常信息包含原始异常消息，方便排查具体问题
            // 抛出ServiceException中断应用启动，包含详细错误信息
            throw new ServiceException("RegionUtils初始化失败，原因：" + e.getMessage());
        }
    }

    /**
     * 根据IP地址离线获取城市信息
     * 通过Searcher查询IP地址对应的地理位置信息
     * 查询性能极高，单次查询约0.1毫秒，支持万级QPS
     *
     * @param ip IP地址字符串（支持IPv4和IPv6格式）
     * @return 城市信息（如"中国|广东|深圳"），查询失败返回"未知"
     *         返回格式：国家|省份|城市|ISP，未知信息用0表示，如"中国|0|深圳|0"
     */
    // 静态方法，方便全局调用，无需创建对象
    // 根据IP地址离线获取城市信息
    public static String getCityInfo(String ip) {
        try {
            // 步骤3：执行IP地址查询
            // StringUtils.trim()去除IP字符串前后的空白字符，防止用户输入带空格的IP导致查询失败
            // SEARCHER.search()执行查询，返回格式如"中国|广东|深圳|电信"
            // 使用StringUtils.trim()去除空白，防止查询失败
            String region = SEARCHER.search(StringUtils.trim(ip));
            // 清理查询结果中的"0|"和"|0"标记
            // ip2region返回的格式中，0表示未知信息，需要过滤掉提升可读性
            // 例如："中国|0|深圳|0" -> "中国|深圳"（中间和末尾的0被移除）
            // 注意：replace()方法会替换所有匹配项，确保彻底清除所有0标记
            // 移除未知信息标记（0），提升结果可读性
            return region.replace("0|", "").replace("|0", "");
        } catch (Exception e) {
            // 捕获查询异常（如IP格式错误、查询器损坏等）
            // 记录错误日志，包含失败的IP地址，方便问题排查和恶意IP分析
            // 日志级别error，表示查询失败需要关注
            // 记录错误日志，包含失败的IP地址，方便问题排查
            log.error("IP地址离线获取城市异常 {}", ip);
            // 查询失败时返回"未知"，避免抛出异常影响业务流程
            // 这种设计保证即使IP查询失败，业务功能也能正常运行
            // 返回"未知"避免影响业务流程，保证系统健壮性
            return "未知";
        }
    }

}

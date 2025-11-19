package org.dromara.common.core.utils.ip;

import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
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
@Slf4j
public class RegionUtils {

    /**
     * IP地址库文件名称
     * 存储在ClassPath下的IP地址数据库文件
     */
    public static final String IP_XDB_FILENAME = "ip2region.xdb";

    /**
     * IP地址查询器
     * 基于ip2region库的Searcher对象，用于查询IP地址信息
     */
    private static final Searcher SEARCHER;

    /**
     * 静态代码块
     * 在类加载时初始化IP地址查询器
     * 将ip2region数据库文件从ClassPath加载到内存并创建Searcher对象
     */
    static {
        try {
            // 1、将ip2region数据库文件xdb从ClassPath加载到内存
            // ResourceUtil.readBytes从ClassPath读取文件字节数据
            // 2、基于加载到内存的xdb数据创建一个Searcher查询对象
            // Searcher.newWithBuffer使用内存中的数据创建查询器
            SEARCHER = Searcher.newWithBuffer(ResourceUtil.readBytes(IP_XDB_FILENAME));
            // 记录初始化成功日志
            log.info("RegionUtils初始化成功，加载IP地址库数据成功！");
        } catch (NoResourceException e) {
            // 处理资源不存在异常：IP地址库文件未找到
            throw new ServiceException("RegionUtils初始化失败，原因：IP地址库数据不存在！");
        } catch (Exception e) {
            // 处理其他异常：文件读取失败、数据格式错误等
            throw new ServiceException("RegionUtils初始化失败，原因：" + e.getMessage());
        }
    }

    /**
     * 根据IP地址离线获取城市信息
     * 通过Searcher查询IP地址对应的地理位置信息
     *
     * @param ip IP地址字符串
     * @return 城市信息（如"中国|广东|深圳"），查询失败返回"未知"
     */
    public static String getCityInfo(String ip) {
        try {
            // 3、执行IP地址查询
            // StringUtils.trim去除IP字符串前后的空白字符
            String region = SEARCHER.search(StringUtils.trim(ip));
            // 清理查询结果中的"0|"和"|0"标记
            // ip2region返回的格式中，0表示未知信息，需要过滤掉
            return region.replace("0|", "").replace("|0", "");
        } catch (Exception e) {
            // 记录查询异常日志，包含失败的IP地址
            log.error("IP地址离线获取城市异常 {}", ip);
            // 查询失败时返回"未知"
            return "未知";
        }
    }

}

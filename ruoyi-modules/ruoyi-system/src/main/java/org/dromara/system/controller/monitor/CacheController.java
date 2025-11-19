// 缓存监控控制器，提供Redis缓存监控和统计功能
package org.dromara.system.controller.monitor;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// Redisson Redis连接工厂，用于获取Redis连接
import org.redisson.spring.data.connection.RedissonConnectionFactory;
// Spring Data Redis连接接口
import org.springframework.data.redis.connection.RedisConnection;
// Redis连接工具类，用于释放连接
import org.springframework.data.redis.core.RedisConnectionUtils;
// GET请求映射注解
import org.springframework.web.bind.annotation.GetMapping;
// 请求路径映射注解
import org.springframework.web.bind.annotation.RequestMapping;
// REST控制器注解
import org.springframework.web.bind.annotation.RestController;

// Java集合工具类
import java.util.*;

/**
 * 缓存监控控制器
 * 提供Redis缓存监控和统计功能，包括命令统计、数据库大小、内存使用等信息
 * 需要monitor:cache:list权限才能访问
 *
 * @author Lion Li
 */
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/monitor/cache
@RequestMapping("/monitor/cache")
public class CacheController {

    // Redisson连接工厂，用于获取Redis连接，自动注入
    private final RedissonConnectionFactory connectionFactory;

    /**
     * 获取缓存监控列表
     * 返回Redis服务器信息、数据库大小、命令统计等监控数据
     * 使用Redis INFO命令获取详细信息
     */
    // Sa-Token权限校验，需要monitor:cache:list权限
    @SaCheckPermission("monitor:cache:list")
    // GET请求映射，路径为/monitor/cache
    @GetMapping()
    public R<CacheListInfoVo> getInfo() throws Exception {
        // 从连接工厂获取Redis连接
        RedisConnection connection = connectionFactory.getConnection();
        try {
            // 获取Redis命令统计信息，返回Properties对象
            Properties commandStats = connection.commands().info("commandstats");
            // 创建饼图数据列表，用于前端展示
            List<Map<String, String>> pieList = new ArrayList<>();
        // 如果命令统计信息不为空
        if (commandStats != null) {
            // 遍历所有命令统计属性
            commandStats.stringPropertyNames().forEach(key -> {
                // 创建包含2个元素的Map，存储命令名称和调用次数
                Map<String, String> data = new HashMap<>(2);
                // 获取属性值
                String property = commandStats.getProperty(key);
                // 移除key的前缀cmdstat_，获取命令名称
                data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                // 从属性值中提取calls=和,usec之间的字符串，获取调用次数
                data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                // 添加到饼图数据列表
                pieList.add(data);
            });
        }
        // 返回成功响应，包含Redis信息、数据库大小和命令统计
        return R.ok(new CacheListInfoVo(
            // 获取Redis服务器所有信息
            connection.commands().info(),
            // 获取数据库key数量
            connection.commands().dbSize(), pieList));
        } finally {
            // 归还连接给连接池，防止连接泄漏
            RedisConnectionUtils.releaseConnection(connection, connectionFactory);
        }
    }

    /**
     * 缓存监控列表信息VO
     * 使用Java Record定义不可变的数据传输对象
     * 包含Redis信息、数据库大小、命令统计三个字段
     *
     * @param info         Redis服务器信息，包含内存、CPU、客户端等
     * @param dbSize       数据库中key的数量
     * @param commandStats 命令统计列表，用于饼图展示
     */
    // Java Record定义，自动生成构造函数、equals、hashCode、toString方法
    public record CacheListInfoVo(Properties info, Long dbSize, List<Map<String, String>> commandStats) {}

}

// 包声明：定义当前类所在的包路径，org.dromara.common.redis.config 表示Redis配置层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.common.redis.config;

// Hutool对象工具类：提供对象判空功能
import cn.hutool.core.util.ObjectUtil;
// Jackson注解：控制JSON序列化行为
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
// Jackson核心类：对象映射器
import com.fasterxml.jackson.databind.ObjectMapper;
// Jackson类型验证器：用于多态类型处理
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
// Jackson Java时间模块：处理Java 8时间类型
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// Jackson反序列化器：处理LocalDateTime
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
// Jackson序列化器：处理LocalDateTime
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
// Lombok日志注解：自动生成日志对象
import lombok.extern.slf4j.Slf4j;
// Spring工具类：获取Spring上下文信息
import org.dromara.common.core.utils.SpringUtils;
// Redisson配置属性类：封装Redisson配置
import org.dromara.common.redis.config.properties.RedissonProperties;
// Redis键前缀处理器：为Redis键添加统一前缀
import org.dromara.common.redis.handler.KeyPrefixHandler;
// Redis异常处理器：统一处理Redis异常
import org.dromara.common.redis.handler.RedisExceptionHandler;
// Redisson字符串编解码器：处理字符串序列化
import org.redisson.client.codec.StringCodec;
// Redisson组合编解码器：组合多种编解码器
import org.redisson.codec.CompositeCodec;
// Redisson JSON编解码器：处理JSON序列化
import org.redisson.codec.TypedJsonJacksonCodec;
// Redisson自动配置定制器：定制Redisson配置
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
// Spring自动注入注解：注入配置属性
import org.springframework.beans.factory.annotation.Autowired;
// Spring自动配置注解：标记为自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// Spring配置属性注解：启用配置属性绑定
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// Spring Bean注解：注册Bean到容器
import org.springframework.context.annotation.Bean;
// Spring虚拟线程任务执行器：支持虚拟线程
import org.springframework.core.task.VirtualThreadTaskExecutor;

// Java时间类：LocalDateTime
import java.time.LocalDateTime;
// Java时间格式化类：DateTimeFormatter
import java.time.format.DateTimeFormatter;
// Java时区类：TimeZone
import java.util.TimeZone;

/**
 * Redis配置类
 * 核心业务：配置Redisson客户端，包括序列化、连接池、编解码器等
 * 支持单机模式和集群模式，可配置key前缀、线程池、超时时间等
 *
 * @author Lion Li
 */
// Lombok日志注解：自动生成slf4j日志对象
@Slf4j
// Spring自动配置注解：标记为自动配置类，在Spring Boot启动时自动加载
@AutoConfiguration
// Spring配置属性注解：启用RedissonProperties配置属性绑定
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisConfig {

    // Spring自动注入：注入Redisson配置属性
    @Autowired
    private RedissonProperties redissonProperties;

    /**
     * Redisson自动配置定制器
     * 用于定制Redisson客户端的配置，包括编解码器、线程池、连接模式等
     * @return RedissonAutoConfigurationCustomizer 配置定制器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        // 返回Lambda表达式，定制Redisson配置
        return config -> {
            // 创建Java时间模块，处理LocalDateTime序列化
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 添加LocalDateTime序列化器
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            // 添加LocalDateTime反序列化器
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            
            // 创建ObjectMapper对象，配置JSON序列化行为
            ObjectMapper om = new ObjectMapper();
            // 注册Java时间模块
            om.registerModule(javaTimeModule);
            // 设置时区为系统默认时区
            om.setTimeZone(TimeZone.getDefault());
            // 设置所有属性的可见性为ANY，允许访问所有属性
            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            // 激活默认类型信息，保存对象全类名，支持多态反序列化
            // 指定序列化输入的类型，类必须是非final修饰的。序列化时将对象全类名一起保存下来
            om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            
            // 注释掉的Fury编解码器配置（高性能序列化框架）
//            LoggerFactory.useSlf4jLogging(true);
//            FuryCodec furyCodec = new FuryCodec();
//            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, furyCodec, furyCodec);
            
            // 创建TypedJsonJacksonCodec编解码器，使用自定义ObjectMapper
            TypedJsonJacksonCodec jsonCodec = new TypedJsonJacksonCodec(Object.class, om);
            // 组合序列化：key使用String，内容使用JSON格式
            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec);
            
            // 配置Redisson线程池数量
            config.setThreads(redissonProperties.getThreads())
                // 配置Netty线程池数量
                .setNettyThreads(redissonProperties.getNettyThreads())
                // 缓存Lua脚本，减少网络传输(redisson大部分功能基于Lua脚本实现)
                .setUseScriptCache(true)
                // 设置编解码器
                .setCodec(codec);
            
            // 如果支持虚拟线程，配置Netty执行器使用虚拟线程
            if (SpringUtils.isVirtual()) {
                config.setNettyExecutor(new VirtualThreadTaskExecutor("redisson-"));
            }
            
            // 获取单机服务器配置
            RedissonProperties.SingleServerConfig singleServerConfig = redissonProperties.getSingleServerConfig();
            // 如果单机配置不为空，使用单机模式
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 使用单机模式配置
                config.useSingleServer()
                    // 设置Redis key前缀
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    // 设置命令超时时间
                    .setTimeout(singleServerConfig.getTimeout())
                    // 设置客户端名称
                    .setClientName(singleServerConfig.getClientName())
                    // 设置连接空闲超时时间
                    .setIdleConnectionTimeout(singleServerConfig.getIdleConnectionTimeout())
                    // 设置发布订阅连接池大小
                    .setSubscriptionConnectionPoolSize(singleServerConfig.getSubscriptionConnectionPoolSize())
                    // 设置最小空闲连接数
                    .setConnectionMinimumIdleSize(singleServerConfig.getConnectionMinimumIdleSize())
                    // 设置连接池大小
                    .setConnectionPoolSize(singleServerConfig.getConnectionPoolSize());
            }
            
            // 获取集群服务器配置
            RedissonProperties.ClusterServersConfig clusterServersConfig = redissonProperties.getClusterServersConfig();
            // 如果集群配置不为空，使用集群模式
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                // 使用集群模式配置
                config.useClusterServers()
                    // 设置Redis key前缀
                    .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                    // 设置命令超时时间
                    .setTimeout(clusterServersConfig.getTimeout())
                    // 设置客户端名称
                    .setClientName(clusterServersConfig.getClientName())
                    // 设置连接空闲超时时间
                    .setIdleConnectionTimeout(clusterServersConfig.getIdleConnectionTimeout())
                    // 设置发布订阅连接池大小
                    .setSubscriptionConnectionPoolSize(clusterServersConfig.getSubscriptionConnectionPoolSize())
                    // 设置master最小空闲连接数
                    .setMasterConnectionMinimumIdleSize(clusterServersConfig.getMasterConnectionMinimumIdleSize())
                    // 设置master连接池大小
                    .setMasterConnectionPoolSize(clusterServersConfig.getMasterConnectionPoolSize())
                    // 设置slave最小空闲连接数
                    .setSlaveConnectionMinimumIdleSize(clusterServersConfig.getSlaveConnectionMinimumIdleSize())
                    // 设置slave连接池大小
                    .setSlaveConnectionPoolSize(clusterServersConfig.getSlaveConnectionPoolSize())
                    // 设置读取模式
                    .setReadMode(clusterServersConfig.getReadMode())
                    // 设置订阅模式
                    .setSubscriptionMode(clusterServersConfig.getSubscriptionMode());
            }
            // 记录日志，Redis配置初始化完成
            log.info("初始化 redis 配置");
        };
    }

    /**
     * Redis异常处理器Bean
     * 用于统一处理Redis相关的异常，返回友好的错误信息
     * @return RedisExceptionHandler 异常处理器
     */
    @Bean
    public RedisExceptionHandler redisExceptionHandler() {
        // 创建并返回Redis异常处理器实例
        return new RedisExceptionHandler();
    }

    /**
     * redis集群配置 yml示例
     * 提供集群配置的参考配置，方便用户配置Redis集群
     *
     * --- # redis 集群配置(单机与集群只能开启一个另一个需要注释掉)
     * spring.data:
     *   redis:
     *     cluster:
     *       nodes:
     *         - 192.168.0.100:6379
     *         - 192.168.0.101:6379
     *         - 192.168.0.102:6379
     *     # 密码
     *     password:
     *     # 连接超时时间
     *     timeout: 10s
     *     # 是否开启ssl
     *     ssl.enabled: false
     *
     * redisson:
     *   # 线程池数量
     *   threads: 16
     *   # Netty线程池数量
     *   nettyThreads: 32
     *   # 集群配置
     *   clusterServersConfig:
     *     # 客户端名称
     *     clientName: ${ruoyi.name}
     *     # master最小空闲连接数
     *     masterConnectionMinimumIdleSize: 32
     *     # master连接池大小
     *     masterConnectionPoolSize: 64
     *     # slave最小空闲连接数
     *     slaveConnectionMinimumIdleSize: 32
     *     # slave连接池大小
     *     slaveConnectionPoolSize: 64
     *     # 连接空闲超时，单位：毫秒
     *     idleConnectionTimeout: 10000
     *     # 命令等待超时，单位：毫秒
     *     timeout: 3000
     *     # 发布和订阅连接池大小
     *     subscriptionConnectionPoolSize: 50
     *     # 读取模式
     *     readMode: "SLAVE"
     *     # 订阅模式
     *     subscriptionMode: "MASTER"
     */

}

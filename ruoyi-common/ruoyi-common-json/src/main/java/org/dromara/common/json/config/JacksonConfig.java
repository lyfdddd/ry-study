package org.dromara.common.json.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.json.handler.BigNumberSerializer;
import org.dromara.common.json.handler.CustomDateDeserializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * jackson 配置
 * 配置Jackson序列化和反序列化规则，解决JavaScript精度丢失问题
 *
 * @author Lion Li
 */
@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {

    /**
     * 注册Java时间模块，配置各种数据类型的序列化方式
     * @return Jackson模块对象
     */
    @Bean
    public Module registerJavaTimeModule() {
        // 创建Java时间模块，用于处理Java 8时间API
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 注册Long类型序列化器，防止JavaScript精度丢失
        javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        // 注册long基本类型序列化器，防止JavaScript精度丢失
        javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        // 注册BigInteger类型序列化器，防止JavaScript精度丢失
        javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
        // 注册BigDecimal类型序列化器，转换为字符串防止精度丢失
        javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        // 创建日期时间格式化器，格式为yyyy-MM-dd HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 注册LocalDateTime序列化器，按指定格式序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        // 注册LocalDateTime反序列化器，按指定格式反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        // 注册Date类型反序列化器，支持多种日期格式
        javaTimeModule.addDeserializer(Date.class, new CustomDateDeserializer());
        return javaTimeModule;
    }

    /**
     * Jackson2对象映射构建器自定义配置
     * @return 自定义配置对象
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 设置时区为系统默认时区
            builder.timeZone(TimeZone.getDefault());
            // 记录日志，提示Jackson配置初始化完成
            log.info("初始化 jackson 配置");
        };
    }

}

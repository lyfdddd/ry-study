package org.dromara.common.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import org.dromara.common.core.factory.YmlPropertySourceFactory;
import org.dromara.common.satoken.core.dao.PlusSaTokenDao;
import org.dromara.common.satoken.core.service.SaPermissionImpl;
import org.dromara.common.satoken.handler.SaTokenExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * sa-token 配置类
 * 配置Sa-Token框架的核心组件，包括Token逻辑、权限接口、持久层和异常处理器
 *
 * @author Lion Li
 */
// @AutoConfiguration注解表示这是一个自动配置类，Spring Boot启动时会自动加载
// @PropertySource指定配置文件位置，使用YmlPropertySourceFactory解析YAML格式
@AutoConfiguration
@PropertySource(value = "classpath:common-satoken.yml", factory = YmlPropertySourceFactory.class)
public class SaTokenConfig {

    /**
     * 配置JWT模式的StpLogic
     * @return StpLogic实例
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        // Sa-Token 整合 jwt (简单模式)
        // 使用JWT作为Token载体，Token中携带用户信息，无需服务端存储
        return new StpLogicJwtForSimple();
    }

    /**
     * 权限接口实现(使用bean注入方便用户替换)
     * @return StpInterface实例
     */
    @Bean
    public StpInterface stpInterface() {
        // 返回自定义的权限实现类，提供菜单和角色权限查询
        return new SaPermissionImpl();
    }

    /**
     * 自定义dao层存储
     * @return SaTokenDao实例
     */
    @Bean
    public SaTokenDao saTokenDao() {
        // 返回自定义的Token持久化实现，使用Redis存储
        return new PlusSaTokenDao();
    }

    /**
     * 异常处理器
     * @return SaTokenExceptionHandler实例
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        // 返回Sa-Token异常处理器，统一处理权限和认证异常
        return new SaTokenExceptionHandler();
    }

}

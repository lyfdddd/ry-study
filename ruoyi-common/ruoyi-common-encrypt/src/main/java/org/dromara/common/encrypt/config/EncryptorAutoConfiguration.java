package org.dromara.common.encrypt.config;

// 导入MyBatis-Plus自动配置类
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
// 导入MyBatis-Plus配置属性
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
// 导入Lombok的Slf4j注解，自动生成日志对象
import lombok.extern.slf4j.Slf4j;
// 导入加密管理器
import org.dromara.common.encrypt.core.EncryptorManager;
// 导入MyBatis解密拦截器
import org.dromara.common.encrypt.interceptor.MybatisDecryptInterceptor;
// 导入MyBatis加密拦截器
import org.dromara.common.encrypt.interceptor.MybatisEncryptInterceptor;
// 导入加密配置属性
import org.dromara.common.encrypt.properties.EncryptorProperties;
// 导入Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// 导入Spring自动配置注解
import org.springframework.boot.autoconfigure.AutoConfiguration;
// 导入条件配置注解
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// 导入启用配置属性注解
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// 导入Bean定义注解
import org.springframework.context.annotation.Bean;

/**
 * 加解密自动配置类
 * 配置MyBatis字段加解密拦截器
 *
 * @author 老马
 * @version 4.6.0
 */
// 标记为Spring自动配置类，在MyBatis-Plus自动配置之后加载
@AutoConfiguration(after = MybatisPlusAutoConfiguration.class)
// 启用EncryptorProperties配置属性
@EnableConfigurationProperties(EncryptorProperties.class)
// 条件配置，当mybatis-encryptor.enable为true时加载
@ConditionalOnProperty(value = "mybatis-encryptor.enable", havingValue = "true")
// Lombok注解，自动生成日志对象
@Slf4j
public class EncryptorAutoConfiguration {

    /**
     * 注入加密配置属性
     */
    @Autowired
    private EncryptorProperties properties;

    /**
     * 创建加密管理器Bean
     * 用于管理加密器实例
     *
     * @param mybatisPlusProperties MyBatis-Plus配置属性
     * @return 加密管理器
     */
    @Bean
    public EncryptorManager encryptorManager(MybatisPlusProperties mybatisPlusProperties) {
        // 创建加密管理器，传入类型别名包路径
        return new EncryptorManager(mybatisPlusProperties.getTypeAliasesPackage());
    }

    /**
     * 创建MyBatis加密拦截器Bean
     * 在数据插入和更新时加密字段
     *
     * @param encryptorManager 加密管理器
     * @return 加密拦截器
     */
    @Bean
    public MybatisEncryptInterceptor mybatisEncryptInterceptor(EncryptorManager encryptorManager) {
        // 创建加密拦截器，传入加密管理器和配置属性
        return new MybatisEncryptInterceptor(encryptorManager, properties);
    }

    /**
     * 创建MyBatis解密拦截器Bean
     * 在数据查询时解密字段
     *
     * @param encryptorManager 加密管理器
     * @return 解密拦截器
     */
    @Bean
    public MybatisDecryptInterceptor mybatisDecryptInterceptor(EncryptorManager encryptorManager) {
        // 创建解密拦截器，传入加密管理器和配置属性
        return new MybatisDecryptInterceptor(encryptorManager, properties);
    }

}




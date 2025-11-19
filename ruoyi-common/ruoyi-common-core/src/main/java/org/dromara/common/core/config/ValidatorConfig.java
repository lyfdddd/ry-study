// 配置Bean Validation校验框架，启用快速失败模式提升性能
package org.dromara.common.core.config;

import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Properties;

/**
 * 校验框架配置类
 * 配置Hibernate Validator为快速失败模式，提升校验性能
 * 该类负责配置JSR 380（Bean Validation 2.0）规范的实现
 * 使用Hibernate Validator作为提供者，支持丰富的校验注解
 * 启用快速失败模式，一旦校验失败立即返回，避免遍历所有字段
 * 支持国际化消息，根据用户的语言环境返回相应的错误提示
 *
 * @author Lion Li
 */
// 在ValidationAutoConfiguration之前加载，确保自定义配置优先
// 这样可以覆盖Spring Boot默认的校验器配置
@AutoConfiguration(before = ValidationAutoConfiguration.class)
public class ValidatorConfig {

    /**
     * 配置校验框架 快速失败模式
     * 使用try-with-resources确保LocalValidatorFactoryBean正确关闭
     * 快速失败模式：一旦某个字段校验失败，立即停止后续校验，提升性能
     * 适用于对性能要求较高的场景，避免不必要的校验开销
     *
     * @param messageSource 国际化消息源，用于获取校验错误提示
     * @return Validator实例，符合JSR 380规范的校验器
     */
    @Bean
    public Validator validator(MessageSource messageSource) {
        // 使用try-with-resources自动关闭资源
        // LocalValidatorFactoryBean实现了AutoCloseable接口，确保资源正确释放
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            // 设置国际化消息源，支持多语言校验提示
            // 会根据用户的Locale返回对应语言的错误消息
            factoryBean.setValidationMessageSource(messageSource);
            
            // 设置使用 HibernateValidator 校验器，功能更强大
            // HibernateValidator是JSR 380规范的参考实现，支持更多校验注解
            factoryBean.setProviderClass(HibernateValidator.class);
            
            // 创建Properties对象，用于配置校验器属性
            Properties properties = new Properties();
            // 设置快速失败模式（fail-fast），即校验过程中一旦遇到失败，立即停止并返回错误
            // 避免遍历所有字段，提升性能
            // 默认值是false，表示会校验所有字段后返回所有错误
            properties.setProperty("hibernate.validator.fail_fast", "true");
            
            // 可以添加更多Hibernate Validator配置
            // 如：properties.setProperty("hibernate.validator.constraint_mapping_contributor", "自定义贡献者");
            
            factoryBean.setValidationProperties(properties);
            // 加载配置，初始化校验器
            // 会读取META-INF/validation.xml等配置文件
            factoryBean.afterPropertiesSet();
            // 返回Validator实例
            return factoryBean.getValidator();
        }
    }

}

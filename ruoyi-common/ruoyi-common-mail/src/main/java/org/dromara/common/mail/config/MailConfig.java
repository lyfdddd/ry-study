package org.dromara.common.mail.config;

// Hutool邮件工具类，封装了JavaMail的复杂操作
import cn.hutool.extra.mail.MailAccount;
// 邮件配置属性类，用于读取application.yml中的配置
import org.dromara.common.mail.config.properties.MailProperties;
// Spring Boot自动配置注解，标识这是一个自动配置类
import org.springframework.boot.autoconfigure.AutoConfiguration;
// Spring Boot条件注解，根据配置项决定是否创建Bean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// Spring Boot配置属性启用注解，启用MailProperties配置绑定
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// Spring Bean注解，将方法返回值注册为Spring容器中的Bean
import org.springframework.context.annotation.Bean;

/**
 * JavaMail 自动配置类
 * 负责根据配置文件创建MailAccount Bean
 * 当mail.enabled=true时才会创建MailAccount
 *
 * @author Michelle.Chung
 */
// Spring Boot自动配置注解：标识这是一个自动配置类
// 会在Spring Boot启动时自动加载并执行配置
@AutoConfiguration
// 启用配置属性：将application.yml中以mail开头的配置项绑定到MailProperties类
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    /**
     * 创建MailAccount Bean
     * MailAccount是Hutool封装的邮件账户对象，包含了SMTP服务器的所有配置信息
     *
     * @param mailProperties 邮件配置属性，从application.yml中读取
     * @return 配置好的MailAccount对象
     */
    // Spring Bean注解：将方法返回值注册为Spring容器中的Bean，供其他组件注入使用
    // 条件注解：只有当mail.enabled配置项的值为true时，才会创建这个Bean
    // 这样可以灵活控制是否启用邮件功能
    @Bean
    @ConditionalOnProperty(value = "mail.enabled", havingValue = "true")
    public MailAccount mailAccount(MailProperties mailProperties) {
        // 创建MailAccount对象，这是Hutool提供的邮件账户封装类
        // 内部封装了JavaMail的Session和Transport等复杂对象
        MailAccount account = new MailAccount();
        
        // 设置SMTP服务器主机地址
        // 例如：smtp.qq.com, smtp.gmail.com等
        account.setHost(mailProperties.getHost());
        
        // 设置SMTP服务器端口
        // 常见端口：25(非加密), 465(SSL), 587(STARTTLS)
        account.setPort(mailProperties.getPort());
        
        // 设置是否需要身份验证
        // 绝大多数SMTP服务器都需要身份验证
        account.setAuth(mailProperties.getAuth());
        
        // 设置发件人邮箱地址
        // 遵循RFC-822标准，可以是"user@qq.com"或"张三 <user@qq.com>"
        account.setFrom(mailProperties.getFrom());
        
        // 设置SMTP登录用户名
        // 通常是邮箱地址或邮箱前缀
        account.setUser(mailProperties.getUser());
        
        // 设置SMTP登录密码或授权码
        // 注意：不是邮箱登录密码，而是SMTP授权码
        account.setPass(mailProperties.getPass());
        
        // 设置SocketFactory端口
        // 通常与SMTP端口相同
        account.setSocketFactoryPort(mailProperties.getPort());
        
        // 是否启用STARTTLS加密
        // STARTTLS将明文连接升级为加密连接
        account.setStarttlsEnable(mailProperties.getStarttlsEnable());
        
        // 是否启用SSL加密
        // SSL全程使用加密传输
        account.setSslEnable(mailProperties.getSslEnable());
        
        // 设置SMTP操作超时时间（毫秒）
        // 防止程序长时间等待无响应
        account.setTimeout(mailProperties.getTimeout());
        
        // 设置Socket连接超时时间（毫秒）
        // 控制与SMTP服务器建立连接的最长时间
        account.setConnectionTimeout(mailProperties.getConnectionTimeout());
        
        // 返回配置好的MailAccount对象
        // 该对象会被Spring容器管理，供MailUtils工具类使用
        return account;
    }

}

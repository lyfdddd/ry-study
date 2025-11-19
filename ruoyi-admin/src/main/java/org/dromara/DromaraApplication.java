package org.dromara;

// Spring Boot应用启动类
import org.springframework.boot.SpringApplication;
// Spring Boot自动配置注解，启用自动配置、组件扫描和配置属性绑定
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Spring Boot应用启动指标缓冲器，用于收集应用启动性能指标
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * RuoYi-Vue-Plus应用启动程序
 * 作为整个系统的入口点，负责初始化Spring Boot应用上下文
 *
 * @author Lion Li
 */

// Spring Boot核心注解：启用自动配置、组件扫描（扫描当前包及子包）、配置属性绑定
@SpringBootApplication
public class DromaraApplication {

    /**
     * 主方法，应用入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 创建SpringApplication实例，指定主配置类
        SpringApplication application = new SpringApplication(DromaraApplication.class);
        // 设置应用启动指标缓冲器，缓冲区大小为2048个事件
        // 用于收集应用启动过程中的性能指标，便于后续分析和优化启动时间
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        // 运行Spring Boot应用，初始化IoC容器、加载配置、启动内嵌Tomcat等
        application.run(args);
        // 打印启动成功提示信息（使用特殊字符美化输出）
        System.out.println("(♥◠‿◠)ﾉﾞ  RuoYi-Vue-Plus启动成功   ლ(´ڡ`ლ)ﾞ");
    }

}

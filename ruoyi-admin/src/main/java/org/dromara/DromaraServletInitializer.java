package org.dromara;

// Spring Boot应用构建器，用于配置和启动Spring Boot应用
import org.springframework.boot.builder.SpringApplicationBuilder;
// Spring Boot Servlet初始化器，用于将应用部署到外部Servlet容器（如Tomcat、Jetty）
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Web容器部署初始化器
 * 用于将Spring Boot应用打包为WAR文件并部署到外部Servlet容器（如Tomcat）
 * 继承SpringBootServletInitializer，重写configure方法配置应用源
 *
 * @author Lion Li
 */
public class DromaraServletInitializer extends SpringBootServletInitializer {

    /**
     * 配置Spring Boot应用源
     * 当应用部署到外部Servlet容器时，此方法会被自动调用
     *
     * @param application SpringApplicationBuilder对象，用于配置应用
     * @return 配置好的SpringApplicationBuilder对象
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // 设置应用的主配置类为DromaraApplication.class
        // 这样Servlet容器启动时会加载Spring Boot应用上下文
        return application.sources(DromaraApplication.class);
    }

}

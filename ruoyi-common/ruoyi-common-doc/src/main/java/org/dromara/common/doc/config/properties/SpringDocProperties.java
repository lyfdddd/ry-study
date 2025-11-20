package org.dromara.common.doc.config.properties;

// 导入OpenAPI组件模型
import io.swagger.v3.oas.models.Components;
// 导入外部文档模型
import io.swagger.v3.oas.models.ExternalDocumentation;
// 导入路径模型
import io.swagger.v3.oas.models.Paths;
// 导入联系人信息模型
import io.swagger.v3.oas.models.info.Contact;
// 导入许可证模型
import io.swagger.v3.oas.models.info.License;
// 导入标签模型
import io.swagger.v3.oas.models.tags.Tag;
// 导入Lombok的Data注解，自动生成getter、setter、toString等方法
import lombok.Data;
// 导入Spring配置属性注解
import org.springframework.boot.context.properties.ConfigurationProperties;
// 导入嵌套配置属性注解
import org.springframework.boot.context.properties.NestedConfigurationProperty;

// 导入List集合接口
import java.util.List;

/**
 * Swagger配置属性类
 * 用于配置API文档的相关属性
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// 指定配置属性的前缀为springdoc
@ConfigurationProperties(prefix = "springdoc")
public class SpringDocProperties {

    /**
     * 文档基本信息
     * 使用嵌套配置属性，映射到InfoProperties对象
     */
    @NestedConfigurationProperty
    private InfoProperties info = new InfoProperties();

    /**
     * 扩展文档地址
     * 使用嵌套配置属性，映射到ExternalDocumentation对象
     */
    @NestedConfigurationProperty
    private ExternalDocumentation externalDocs;

    /**
     * 标签列表
     * 用于对API进行分类
     */
    private List<Tag> tags = null;

    /**
     * 路径
     * 使用嵌套配置属性，映射到Paths对象
     */
    @NestedConfigurationProperty
    private Paths paths = null;

    /**
     * 组件
     * 包含安全方案、请求体、响应等组件定义
     * 使用嵌套配置属性，映射到Components对象
     */
    @NestedConfigurationProperty
    private Components components = null;

    /**
     * <p>
     * 文档的基础属性信息
     * </p>
     *
     * @see io.swagger.v3.oas.models.info.Info
     *
     * 为了 springboot 自动生产配置提示信息，所以这里复制一个类出来
     * 内部静态类，用于配置文档基本信息
     */
    @Data
    public static class InfoProperties {

        /**
         * 标题
         * API文档的标题
         */
        private String title = null;

        /**
         * 描述
         * API文档的描述信息
         */
        private String description = null;

        /**
         * 联系人信息
         * 使用嵌套配置属性，映射到Contact对象
         */
        @NestedConfigurationProperty
        private Contact contact = null;

        /**
         * 许可证
         * 使用嵌套配置属性，映射到License对象
         */
        @NestedConfigurationProperty
        private License license = null;

        /**
         * 版本
         * API文档的版本号
         */
        private String version = null;

    }

}

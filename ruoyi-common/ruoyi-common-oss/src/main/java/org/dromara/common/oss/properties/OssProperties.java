// 定义OSS配置属性类的包路径
package org.dromara.common.oss.properties;

// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

/**
 * OSS对象存储配置属性类
 * 封装OSS连接和操作的配置信息，包括访问密钥、端点、桶名称等
 * 使用Lombok @Data注解简化代码
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// OSS配置属性类
public class OssProperties {

    /**
     * 租户ID
     * 用于多租户场景下的OSS配置隔离
     */
    private String tenantId;

    /**
     * OSS服务端点（Endpoint）
     * OSS服务的访问地址，如：oss-cn-hangzhou.aliyuncs.com
     */
    private String endpoint;

    /**
     * 自定义域名
     * 如果配置了自定义域名，则使用自定义域名访问OSS资源
     */
    private String domain;

    /**
     * 文件存储路径前缀
     * 上传文件时自动添加的前缀路径，用于分类管理
     */
    private String prefix;

    /**
     * 访问密钥ID（Access Key ID）
     * 用于身份验证的公钥
     */
    private String accessKey;

    /**
     * 访问密钥Secret（Access Key Secret）
     * 用于身份验证的私钥，需保密
     */
    private String secretKey;

    /**
     * 存储桶名称（Bucket Name）
     * OSS中的存储空间名称
     */
    private String bucketName;

    /**
     * 存储区域（Region）
     * OSS服务所在的区域，如：cn-hangzhou
     */
    private String region;

    /**
     * 是否使用HTTPS协议
     * Y=是，N=否
     */
    private String isHttps;

    /**
     * 存储桶权限类型
     * 0=私有（private），1=公共读（public），2=自定义（custom）
     */
    private String accessPolicy;

}

// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;
// MyBatis-Plus基础实体类：提供创建时间、更新时间、创建人、更新人等公共字段
// BaseEntity是MyBatis-Plus提供的实体基类，包含审计字段
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 对象存储配置对象 sys_oss_config
 * 继承BaseEntity，继承审计相关的基础字段（createBy、createTime、updateBy、updateTime）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_oss_config表，存储OSS配置信息
 * 用于管理不同OSS服务商的配置参数
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_oss_config
@TableName("sys_oss_config")
// 对象存储配置实体类，继承基础实体类
public class SysOssConfig extends BaseEntity {

    /**
     * 主键
     * 主键字段，使用@TableId注解标记
     * 对应数据库的oss_config_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为oss_config_id
    @TableId(value = "oss_config_id")
    // 主键ID，Long类型
    private Long ossConfigId;

    /**
     * 配置key
     * OSS配置的唯一标识key
     * 用于区分不同的OSS配置
     * 对应数据库的config_key字段
     */
    // 配置key，String类型
    private String configKey;

    /**
     * accessKey
     * OSS访问密钥ID
     * 对应数据库的access_key字段
     */
    // accessKey，String类型
    private String accessKey;

    /**
     * 秘钥
     * OSS访问密钥Secret
     * 对应数据库的secret_key字段
     */
    // 秘钥，String类型
    private String secretKey;

    /**
     * 桶名称
     * OSS存储桶名称
     * 对应数据库的bucket_name字段
     */
    // 桶名称，String类型
    private String bucketName;

    /**
     * 前缀
     * 文件存储前缀路径
     * 用于在桶内创建子目录
     * 对应数据库的prefix字段
     */
    // 前缀，String类型
    private String prefix;

    /**
     * 访问站点
     * OSS服务访问站点地址
     * 如：oss-cn-hangzhou.aliyuncs.com
     * 对应数据库的endpoint字段
     */
    // 访问站点，String类型
    private String endpoint;

    /**
     * 自定义域名
     * 自定义访问域名，用于CDN加速
     * 对应数据库的domain字段
     */
    // 自定义域名，String类型
    private String domain;

    /**
     * 是否https（0否 1是）
     * 是否使用HTTPS协议访问
     * 0表示否，1表示是
     * 对应数据库的is_https字段
     */
    // 是否https，String类型
    private String isHttps;

    /**
     * 域
     * OSS服务区域，如cn-hangzhou
     * 对应数据库的region字段
     */
    // 域，String类型
    private String region;

    /**
     * 是否默认（0=是,1=否）
     * 是否为默认配置
     * 0表示是，1表示否
     * 对应数据库的status字段
     */
    // 是否默认，String类型
    private String status;

    /**
     * 扩展字段
     * 预留扩展字段，用于存储额外信息
     * 对应数据库的ext1字段
     */
    // 扩展字段，String类型
    private String ext1;

    /**
     * 备注
     * 配置备注信息，用于记录说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

    /**
     * 桶权限类型(0private 1public 2custom)
     * 存储桶访问权限类型
     * 0表示私有，1表示公有读，2表示自定义权限
     * 对应数据库的access_policy字段
     */
    // 桶权限类型，String类型
    private String accessPolicy;
}

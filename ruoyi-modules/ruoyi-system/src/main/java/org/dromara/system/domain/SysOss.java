// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// 租户实体基类：提供租户相关的基础字段和功能
// TenantEntity是租户模块提供的实体基类，包含租户ID等公共字段
import org.dromara.common.tenant.core.TenantEntity;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * OSS对象存储对象
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_oss表，存储文件上传信息
 * 用于管理上传到OSS的文件元数据
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_oss
@TableName("sys_oss")
// 对象存储实体类，继承租户实体基类
public class SysOss extends TenantEntity {

    /**
     * 对象存储主键
     * 主键字段，使用@TableId注解标记
     * 对应数据库的oss_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为oss_id
    @TableId(value = "oss_id")
    // 对象存储主键，Long类型
    private Long ossId;

    /**
     * 文件名
     * 文件存储后的文件名，通常是UUID或时间戳命名
     * 对应数据库的file_name字段
     */
    // 文件名，String类型
    private String fileName;

    /**
     * 原名
     * 文件原始名称，用户上传时的文件名
     * 用于下载时显示原始文件名
     * 对应数据库的original_name字段
     */
    // 原名，String类型
    private String originalName;

    /**
     * 文件后缀名
     * 文件扩展名，如.jpg、.png、.pdf等
     * 用于判断文件类型
     * 对应数据库的file_suffix字段
     */
    // 文件后缀名，String类型
    private String fileSuffix;

    /**
     * URL地址
     * 文件访问URL地址，可直接访问
     * 对应数据库的url字段
     */
    // URL地址，String类型
    private String url;

    /**
     * 扩展字段
     * 预留扩展字段，用于存储额外信息
     * 对应数据库的ext1字段
     */
    // 扩展字段，String类型
    private String ext1;

    /**
     * 服务商
     * OSS服务商名称，如aliyun、tencent、qiniu等
     * 对应数据库的service字段
     */
    // 服务商，String类型
    private String service;

}

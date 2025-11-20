// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，@TableLogic标记逻辑删除字段
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
// MyBatis-Plus基础实体类：提供创建时间、更新时间、创建人、更新人等公共字段
// BaseEntity是MyBatis-Plus提供的实体基类，包含审计字段
import org.dromara.common.mybatis.core.domain.BaseEntity;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;

// 序列化接口：提供序列化支持
import java.io.Serial;
// Date类：用于处理日期时间
import java.util.Date;

/**
 * 租户对象 sys_tenant
 * 继承BaseEntity，继承审计相关的基础字段（createBy、createTime、updateBy、updateTime）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_tenant表，存储租户信息
 * 用于实现SaaS多租户架构，每个租户拥有独立的数据空间
 * 包含租户基本信息、套餐、过期时间等
 *
 * @author Michelle.Chung
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_tenant
@TableName("sys_tenant")
// 租户实体类，继承基础实体类
public class SysTenant extends BaseEntity {

    // 序列化版本UID，用于版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     * 主键字段，使用@TableId注解标记
     * 对应数据库的id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为id
    @TableId(value = "id")
    // 主键ID，Long类型
    private Long id;

    /**
     * 租户编号
     * 租户的唯一标识编号
     * 用于区分不同租户，如TENANT001、TENANT002
     * 对应数据库的tenant_id字段
     */
    // 租户编号，String类型
    private String tenantId;

    /**
     * 联系人
     * 租户联系人的姓名
     * 对应数据库的contact_user_name字段
     */
    // 联系人，String类型
    private String contactUserName;

    /**
     * 联系电话
     * 租户联系人的电话号码
     * 对应数据库的contact_phone字段
     */
    // 联系电话，String类型
    private String contactPhone;

    /**
     * 企业名称
     * 租户企业的全称
     * 对应数据库的company_name字段
     */
    // 企业名称，String类型
    private String companyName;

    /**
     * 统一社会信用代码
     * 企业的统一社会信用代码，唯一标识企业
     * 对应数据库的license_number字段
     */
    // 统一社会信用代码，String类型
    private String licenseNumber;

    /**
     * 地址
     * 企业的注册地址或办公地址
     * 对应数据库的address字段
     */
    // 地址，String类型
    private String address;

    /**
     * 域名
     * 租户的自定义域名
     * 用于实现域名级别的租户隔离
     * 对应数据库的domain字段
     */
    // 域名，String类型
    private String domain;

    /**
     * 企业简介
     * 企业的简介信息
     * 对应数据库的intro字段
     */
    // 企业简介，String类型
    private String intro;

    /**
     * 备注
     * 租户的备注信息
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

    /**
     * 租户套餐编号
     * 关联的租户套餐ID
     * 用于指定租户的功能权限
     * 对应数据库的package_id字段
     */
    // 租户套餐编号，Long类型
    private Long packageId;

    /**
     * 过期时间
     * 租户套餐的过期时间
     * 用于控制租户服务有效期
     * 对应数据库的expire_time字段
     */
    // 过期时间，Date类型
    private Date expireTime;

    /**
     * 用户数量（-1不限制）
     * 租户允许的最大用户数量
     * -1表示不限制用户数量
     * 对应数据库的account_count字段
     */
    // 用户数量，Long类型
    private Long accountCount;

    /**
     * 租户状态（0正常 1停用）
     * 租户状态，0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 租户状态，String类型
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     * 逻辑删除标志，0表示存在，1表示已删除
     * 使用@TableLogic注解标记为逻辑删除字段
     * 对应数据库的del_flag字段
     */
    // MyBatis-Plus注解：标记为逻辑删除字段
    @TableLogic
    // 删除标志，String类型
    private String delFlag;

}

// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法
import lombok.Data;

// 序列化接口：提供序列化支持
import java.io.Serial;
import java.io.Serializable;
// Date类：用于处理日期时间
import java.util.Date;

/**
 * 系统访问记录表 sys_logininfor
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_logininfor表，存储用户登录日志信息
 * 用于记录用户的登录行为，包括登录时间、IP、设备、状态等
 * 这是一个日志实体，没有继承BaseEntity，因为日志通常不需要审计字段
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// MyBatis-Plus注解：指定对应的数据库表名为sys_logininfor
@TableName("sys_logininfor")
// 系统访问记录实体类，实现序列化接口
public class SysLogininfor implements Serializable {

    // 序列化版本UID，用于版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     * 主键字段，使用@TableId注解标记
     * 对应数据库的info_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为info_id
    @TableId(value = "info_id")
    // 主键ID，Long类型
    private Long infoId;

    /**
     * 租户编号
     * 租户ID，用于多租户隔离
     * 对应数据库的tenant_id字段
     */
    // 租户编号，String类型
    private String tenantId;

    /**
     * 用户账号
     * 登录的用户账号
     * 对应数据库的user_name字段
     */
    // 用户账号，String类型
    private String userName;

    /**
     * 客户端
     * 客户端标识key
     * 对应数据库的client_key字段
     */
    // 客户端，String类型
    private String clientKey;

    /**
     * 设备类型
     * 登录设备类型，如pc、app等
     * 对应数据库的device_type字段
     */
    // 设备类型，String类型
    private String deviceType;

    /**
     * 登录状态 0成功 1失败
     * 登录结果状态，0表示成功，1表示失败
     * 对应数据库的status字段
     */
    // 登录状态，String类型
    private String status;

    /**
     * 登录IP地址
     * 用户登录时的IP地址
     * 对应数据库的ipaddr字段
     */
    // 登录IP地址，String类型
    private String ipaddr;

    /**
     * 登录地点
     * IP地址解析的地理位置
     * 如：浙江省杭州市
     * 对应数据库的login_location字段
     */
    // 登录地点，String类型
    private String loginLocation;

    /**
     * 浏览器类型
     * 用户登录时使用的浏览器类型
     * 如：Chrome、Firefox、Safari等
     * 对应数据库的browser字段
     */
    // 浏览器类型，String类型
    private String browser;

    /**
     * 操作系统
     * 用户登录时使用的操作系统
     * 如：Windows 10、macOS、Android等
     * 对应数据库的os字段
     */
    // 操作系统，String类型
    private String os;

    /**
     * 提示消息
     * 登录结果的提示消息
     * 如：登录成功、密码错误、账号锁定等
     * 对应数据库的msg字段
     */
    // 提示消息，String类型
    private String msg;

    /**
     * 访问时间
     * 登录发生的时间
     * 对应数据库的login_time字段
     */
    // 访问时间，Date类型
    private Date loginTime;

}

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
 * 通知公告表 sys_notice
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_notice表，存储系统通知公告信息
 * 用于发布系统通知、公告、消息等
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_notice
@TableName("sys_notice")
// 通知公告实体类，继承租户实体基类
public class SysNotice extends TenantEntity {

    /**
     * 公告ID
     * 主键字段，使用@TableId注解标记
     * 对应数据库的notice_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为notice_id
    @TableId(value = "notice_id")
    // 公告ID，Long类型
    private Long noticeId;

    /**
     * 公告标题
     * 通知公告的标题，用于界面展示
     * 对应数据库的notice_title字段
     */
    // 公告标题，String类型
    private String noticeTitle;

    /**
     * 公告类型（1通知 2公告）
     * 公告类型，区分通知和公告
     * 1表示通知，2表示公告
     * 对应数据库的notice_type字段
     */
    // 公告类型，String类型
    private String noticeType;

    /**
     * 公告内容
     * 通知公告的详细内容
     * 支持HTML格式，用于富文本展示
     * 对应数据库的notice_content字段
     */
    // 公告内容，String类型
    private String noticeContent;

    /**
     * 公告状态（0正常 1关闭）
     * 公告状态，控制公告是否显示
     * 0表示正常显示，1表示已关闭
     * 对应数据库的status字段
     */
    // 公告状态，String类型
    private String status;

    /**
     * 备注
     * 公告备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

}

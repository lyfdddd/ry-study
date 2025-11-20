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
 * 岗位表 sys_post
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_post表，存储岗位信息
 * 用于管理系统中的岗位（职位）数据
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_post
@TableName("sys_post")
// 岗位实体类，继承租户实体基类
public class SysPost extends TenantEntity {

    /**
     * 岗位序号
     * 主键字段，使用@TableId注解标记
     * 对应数据库的post_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为post_id
    @TableId(value = "post_id")
    // 岗位序号，Long类型
    private Long postId;

    /**
     * 部门id
     * 关联的部门ID，表示岗位所属的部门
     * 对应数据库的dept_id字段
     */
    // 部门id，Long类型
    private Long deptId;

    /**
     * 岗位编码
     * 岗位的唯一编码，用于业务标识
     * 如：CEO、CTO、DEV等
     * 对应数据库的post_code字段
     */
    // 岗位编码，String类型
    private String postCode;

    /**
     * 岗位名称
     * 岗位的显示名称
     * 如：总经理、技术总监、开发工程师等
     * 对应数据库的post_name字段
     */
    // 岗位名称，String类型
    private String postName;

    /**
     * 岗位类别编码
     * 岗位类别编码，用于分类管理
     * 对应数据库的post_category字段
     */
    // 岗位类别编码，String类型
    private String postCategory;

    /**
     * 岗位排序
     * 岗位排序号，用于界面展示顺序
     * 数值越小越靠前
     * 对应数据库的post_sort字段
     */
    // 岗位排序，Integer类型
    private Integer postSort;

    /**
     * 状态（0正常 1停用）
     * 岗位状态，0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 状态，String类型
    private String status;

    /**
     * 备注
     * 岗位备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

}

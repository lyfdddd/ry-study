// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，@TableLogic指定逻辑删除字段，@TableField指定字段属性
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;
// 租户实体基类：提供租户相关的基础字段和功能
// TenantEntity是租户模块提供的实体基类，包含租户ID等公共字段
import org.dromara.common.tenant.core.TenantEntity;

// Java序列化接口
// Serial是Java 14+提供的序列化接口标记，用于序列化版本控制
import java.io.Serial;
// Java列表接口和实现类
// List是列表接口，ArrayList是列表实现类
import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_dept表，存储系统部门信息
 * 支持树形结构，通过parentId和ancestors字段实现部门层级关系
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_dept
@TableName("sys_dept")
// 部门实体类，继承租户实体基类
public class SysDept extends TenantEntity {

    // Java序列化版本UID，用于序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     * 主键字段，使用@TableId注解标记
     * 对应数据库的dept_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为dept_id
    @TableId(value = "dept_id")
    // 部门ID，Long类型
    private Long deptId;

    /**
     * 父部门ID
     * 父部门的外键，用于构建部门树形结构
     * 顶级部门的parentId为0
     * 对应数据库的parent_id字段
     */
    // 父部门ID，Long类型
    private Long parentId;

    /**
     * 部门名称
     * 部门的显示名称，用于界面展示
     * 对应数据库的dept_name字段
     */
    // 部门名称，String类型
    private String deptName;

    /**
     * 部门类别编码
     * 部门的类别编码，用于分类管理
     * 对应数据库的dept_category字段
     */
    // 部门类别编码，String类型
    private String deptCategory;

    /**
     * 显示顺序
     * 部门的排序号，用于界面显示顺序
     * 对应数据库的order_num字段
     */
    // 显示顺序，Integer类型
    private Integer orderNum;

    /**
     * 负责人
     * 部门负责人的用户ID
     * 对应数据库的leader字段
     */
    // 负责人用户ID，Long类型
    private Long leader;

    /**
     * 联系电话
     * 部门的联系电话
     * 对应数据库的phone字段
     */
    // 联系电话，String类型
    private String phone;

    /**
     * 邮箱
     * 部门的邮箱地址
     * 对应数据库的email字段
     */
    // 邮箱，String类型
    private String email;

    /**
     * 部门状态:0正常,1停用
     * 部门状态，控制部门是否可用
     * 0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 部门状态，String类型
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     * 逻辑删除标志，使用@TableLogic注解标记
     * 0表示存在，1表示已删除
     * 对应数据库的del_flag字段
     */
    // MyBatis-Plus注解：标记为逻辑删除字段
    @TableLogic
    // 删除标志，String类型
    private String delFlag;

    /**
     * 祖级列表
     * 部门的所有祖先ID列表，格式如：0,1,2,3
     * 用于快速查询部门及其所有子部门
     * 对应数据库的ancestors字段
     */
    // 祖级列表，String类型
    private String ancestors;

    /**
     * 子部门
     * 部门的子部门列表，用于树形结构展示
     * 使用@TableField(exist = false)标记为非数据库字段
     * 通过关联查询动态填充
     */
    // MyBatis-Plus注解：标记为非数据库字段，exist=false表示该字段不存在于数据库表中
    @TableField(exist = false)
    // 子部门列表，List类型，初始化为空列表
    private List<SysDept> children = new ArrayList<>();

}

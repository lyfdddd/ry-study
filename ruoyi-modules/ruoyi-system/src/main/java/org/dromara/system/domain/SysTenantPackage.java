// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，@TableLogic标记逻辑删除字段
import com.baomidou.mybatisplus.annotation.*;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;
// 序列化接口：提供序列化支持
import java.io.Serial;

// MyBatis-Plus基础实体类：提供创建时间、更新时间、创建人、更新人等公共字段
// BaseEntity是MyBatis-Plus提供的实体基类，包含审计字段
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 租户套餐对象 sys_tenant_package
 * 继承BaseEntity，继承审计相关的基础字段（createBy、createTime、updateBy、updateTime）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_tenant_package表，存储租户套餐信息
 * 用于定义不同租户套餐的功能权限，如基础版、高级版、企业版等
 * 套餐可以关联菜单，控制租户可见的功能菜单
 *
 * @author Michelle.Chung
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_tenant_package
@TableName("sys_tenant_package")
// 租户套餐实体类，继承基础实体类
public class SysTenantPackage extends BaseEntity {

    // 序列化版本UID，用于版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户套餐id
     * 主键字段，使用@TableId注解标记
     * 对应数据库的package_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为package_id
    @TableId(value = "package_id")
    // 租户套餐id，Long类型
    private Long packageId;

    /**
     * 套餐名称
     * 租户套餐的名称，如基础版、高级版、企业版
     * 对应数据库的package_name字段
     */
    // 套餐名称，String类型
    private String packageName;

    /**
     * 关联菜单id
     * 套餐关联的菜单ID列表，多个ID用逗号分隔
     * 用于控制租户可见的菜单功能
     * 对应数据库的menu_ids字段
     */
    // 关联菜单id，String类型
    private String menuIds;

    /**
     * 备注
     * 套餐的备注信息
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     * 菜单树选择时的关联显示策略
     * true表示父子节点互相关联，false表示不关联
     * 对应数据库的menu_check_strictly字段
     */
    // 菜单树选择项是否关联显示，Boolean类型
    private Boolean menuCheckStrictly;

    /**
     * 状态（0正常 1停用）
     * 套餐状态，0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 状态，String类型
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

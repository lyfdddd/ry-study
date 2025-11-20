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
 * 字典类型表 sys_dict_type
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_dict_type表，存储字典类型信息
 * 字典类型用于对字典数据进行分类管理，如用户性别、是否选项等
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_dict_type
@TableName("sys_dict_type")
// 字典类型实体类，继承租户实体基类
public class SysDictType extends TenantEntity {

    /**
     * 字典主键
     * 主键字段，使用@TableId注解标记
     * 对应数据库的dict_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为dict_id
    @TableId(value = "dict_id")
    // 字典主键，Long类型
    private Long dictId;

    /**
     * 字典名称
     * 字典类型的显示名称，用于界面展示
     * 如：用户性别、系统是否、通知类型等
     * 对应数据库的dict_name字段
     */
    // 字典名称，String类型
    private String dictName;

    /**
     * 字典类型
     * 字典类型的唯一标识，用于关联字典数据
     * 如：sys_user_sex、sys_yes_no、sys_notice_type等
     * 对应数据库的dict_type字段
     */
    // 字典类型，String类型
    private String dictType;

    /**
     * 备注
     * 字典类型备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

}

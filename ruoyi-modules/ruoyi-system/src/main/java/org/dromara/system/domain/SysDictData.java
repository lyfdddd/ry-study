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
// 系统常量类：系统相关常量定义
// SystemConstants包含系统级别的常量，如YES、NO等
import org.dromara.common.core.constant.SystemConstants;
// 租户实体基类：提供租户相关的基础字段和功能
// TenantEntity是租户模块提供的实体基类，包含租户ID等公共字段
import org.dromara.common.tenant.core.TenantEntity;

/**
 * 字典数据表 sys_dict_data
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_dict_data表，存储字典数据信息
 * 字典数据用于系统配置和枚举值管理，如下拉框选项等
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_dict_data
@TableName("sys_dict_data")
// 字典数据实体类，继承租户实体基类
public class SysDictData extends TenantEntity {

    /**
     * 字典编码
     * 主键字段，使用@TableId注解标记
     * 对应数据库的dict_code字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为dict_code
    @TableId(value = "dict_code")
    // 字典编码，Long类型
    private Long dictCode;

    /**
     * 字典排序
     * 字典数据的排序号，用于界面显示顺序
     * 对应数据库的dict_sort字段
     */
    // 字典排序，Integer类型
    private Integer dictSort;

    /**
     * 字典标签
     * 字典数据的显示标签，用于界面展示
     * 如：男、女；是、否等
     * 对应数据库的dict_label字段
     */
    // 字典标签，String类型
    private String dictLabel;

    /**
     * 字典键值
     * 字典数据的键值，用于程序逻辑判断
     * 如：0、1；Y、N等
     * 对应数据库的dict_value字段
     */
    // 字典键值，String类型
    private String dictValue;

    /**
     * 字典类型
     * 字典类型标识，关联字典类型表
     * 如：sys_user_sex、sys_yes_no等
     * 对应数据库的dict_type字段
     */
    // 字典类型，String类型
    private String dictType;

    /**
     * 样式属性（其他样式扩展）
     * 字典数据的样式属性，用于前端样式控制
     * 对应数据库的css_class字段
     */
    // 样式属性，String类型
    private String cssClass;

    /**
     * 表格字典样式
     * 表格中字典数据的样式类
     * 如：primary、success、warning、danger等
     * 对应数据库的list_class字段
     */
    // 表格字典样式，String类型
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     * 是否默认标志，控制是否作为默认值
     * Y表示是，N表示否
     * 对应数据库的is_default字段
     */
    // 是否默认，String类型
    private String isDefault;

    /**
     * 备注
     * 字典数据备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

    /**
     * 判断是否默认
     * 通过比较isDefault字段是否等于系统常量YES
     * 用于判断该字典数据是否为默认选项
     *
     * @return 如果是默认返回true，否则返回false
     */
    // 判断是否默认方法
    public boolean getDefault() {
        // 比较isDefault字段是否等于系统常量YES
        return SystemConstants.YES.equals(this.isDefault);
    }

}

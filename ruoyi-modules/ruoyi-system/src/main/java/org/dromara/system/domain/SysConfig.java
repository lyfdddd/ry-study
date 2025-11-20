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
 * 参数配置表 sys_config
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_config表，存储系统参数配置信息
 * 参数配置用于系统运行时动态配置，如系统设置、业务规则等
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_config
@TableName("sys_config")
// 参数配置实体类，继承租户实体基类
public class SysConfig extends TenantEntity {

    /**
     * 参数主键
     * 主键字段，使用@TableId注解标记
     * 对应数据库的config_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为config_id
    @TableId(value = "config_id")
    // 参数主键，Long类型
    private Long configId;

    /**
     * 参数名称
     * 参数配置的显示名称，用于界面展示
     * 如：系统名称、默认密码、用户注册开关等
     * 对应数据库的config_name字段
     */
    // 参数名称，String类型
    private String configName;

    /**
     * 参数键名
     * 参数配置的唯一标识，用于程序中获取配置值
     * 如：sys.user.initPassword、sys.account.registerUser等
     * 对应数据库的config_key字段
     */
    // 参数键名，String类型
    private String configKey;

    /**
     * 参数键值
     * 参数配置的值，根据键名获取对应的值
     * 可以是字符串、数字、布尔值等
     * 对应数据库的config_value字段
     */
    // 参数键值，String类型
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     * 系统内置标志，控制参数是否可编辑
     * Y表示是系统内置参数，不可删除和修改键名
     * N表示非系统内置参数，可以删除和修改
     * 对应数据库的config_type字段
     */
    // 系统内置标志，String类型
    private String configType;

    /**
     * 备注
     * 参数配置备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

}

// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，IdType指定主键生成策略
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法
import lombok.Data;

/**
 * 角色和菜单关联 sys_role_menu
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_role_menu表，存储角色与菜单的关联关系
 * 用于实现权限控制，指定角色可以访问哪些菜单
 * 这是一个关联表实体，没有继承TenantEntity，因为关联关系通常不区分租户
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// MyBatis-Plus注解：指定对应的数据库表名为sys_role_menu
@TableName("sys_role_menu")
// 角色和菜单关联实体类
public class SysRoleMenu {

    /**
     * 角色ID
     * 关联的角色ID，使用@TableId注解标记
     * type = IdType.INPUT表示主键值由用户输入，不自动生成
     * 对应数据库的role_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，type=INPUT表示手动输入主键值
    @TableId(type = IdType.INPUT)
    // 角色ID，Long类型
    private Long roleId;

    /**
     * 菜单ID
     * 关联的菜单ID
     * 对应数据库的menu_id字段
     */
    // 菜单ID，Long类型
    private Long menuId;

}

// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，@TableLogic指定逻辑删除字段
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
// 租户实体基类：提供租户相关的基础字段和功能
// TenantEntity是租户模块提供的实体基类，包含租户ID等公共字段
import org.dromara.common.tenant.core.TenantEntity;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode，@NoArgsConstructor生成无参构造
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色表 sys_role
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_role表，存储系统角色信息
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_role
@TableName("sys_role")
// 角色实体类，继承租户实体基类
public class SysRole extends TenantEntity {

    /**
     * 角色ID
     * 主键字段，使用@TableId注解标记
     * 对应数据库的role_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为role_id
    @TableId(value = "role_id")
    // 角色ID，Long类型
    private Long roleId;

    /**
     * 角色名称
     * 角色的显示名称，用于界面展示
     * 对应数据库的role_name字段
     */
    // 角色名称，String类型
    private String roleName;

    /**
     * 角色权限
     * 角色的权限标识，用于权限控制
     * 如：admin、common等
     * 对应数据库的role_key字段
     */
    // 角色权限标识，String类型
    private String roleKey;

    /**
     * 角色排序
     * 角色的排序号，用于界面显示顺序
     * 对应数据库的role_sort字段
     */
    // 角色排序，Integer类型
    private Integer roleSort;

    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）
     * 角色的数据权限范围，控制角色能访问的数据范围
     * 1-全部数据：可以查看所有数据
     * 2-自定数据权限：可以查看自定义的数据
     * 3-本部门数据权限：只能查看本部门数据
     * 4-本部门及以下数据权限：可以查看本部门及子部门数据
     * 5-仅本人数据权限：只能查看自己的数据
     * 6-部门及以下或本人数据权限：可以查看本部门及以下或本人的数据
     * 对应数据库的data_scope字段
     */
    // 数据范围，String类型
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     * 控制菜单树形结构的选择行为
     * true表示父子节点关联选择，false表示独立选择
     * 对应数据库的menu_check_strictly字段
     */
    // 菜单树选择项是否关联显示，Boolean类型
    private Boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     * 控制部门树形结构的选择行为
     * true表示父子节点关联选择，false表示独立选择
     * 对应数据库的dept_check_strictly字段
     */
    // 部门树选择项是否关联显示，Boolean类型
    private Boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     * 角色状态，控制角色是否可用
     * 0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 角色状态，String类型
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
     * 备注
     * 角色备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;

    /**
     * 带参构造函数
     * 根据角色ID创建角色对象
     *
     * @param roleId 角色ID
     */
    // 带参构造函数，接收角色ID参数
    public SysRole(Long roleId) {
        // 初始化角色ID
        this.roleId = roleId;
    }

}

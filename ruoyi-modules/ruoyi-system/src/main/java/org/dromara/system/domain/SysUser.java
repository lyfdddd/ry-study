// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键，@TableLogic指定逻辑删除字段，@TableField指定字段策略
import com.baomidou.mybatisplus.annotation.*;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode，@NoArgsConstructor生成无参构造
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
// 系统常量类：系统相关常量定义
// SystemConstants包含系统级别的常量，如超级管理员ID等
import org.dromara.common.core.constant.SystemConstants;
// 租户实体基类：提供租户相关的基础字段和功能
// TenantEntity是租户模块提供的实体基类，包含租户ID等公共字段
import org.dromara.common.tenant.core.TenantEntity;

// Java日期类
// Date用于表示日期和时间，存储最后登录时间
import java.util.Date;

/**
 * 用户对象 sys_user
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_user表，存储系统用户信息
 *
 * @author Lion Li
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_user
@TableName("sys_user")
// 用户实体类，继承租户实体基类
public class SysUser extends TenantEntity {

    /**
     * 用户ID
     * 主键字段，使用@TableId注解标记
     * 对应数据库的user_id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为user_id
    @TableId(value = "user_id")
    // 用户ID，Long类型
    private Long userId;

    /**
     * 部门ID
     * 用户所属部门的外键
     * 对应数据库的dept_id字段
     */
    // 部门ID，Long类型
    private Long deptId;

    /**
     * 用户账号
     * 用户登录系统的账号名，唯一标识
     * 对应数据库的user_name字段
     */
    // 用户账号，String类型
    private String userName;

    /**
     * 用户昵称
     * 用户显示名称，用于界面展示
     * 对应数据库的nick_name字段
     */
    // 用户昵称，String类型
    private String nickName;

    /**
     * 用户类型（sys_user系统用户）
     * 区分用户类型，如系统用户、其他类型用户
     * 对应数据库的user_type字段
     */
    // 用户类型，String类型
    private String userType;

    /**
     * 用户邮箱
     * 用户邮箱地址，用于邮件通知和密码找回
     * 对应数据库的email字段
     */
    // 用户邮箱，String类型
    private String email;

    /**
     * 手机号码
     * 用户手机号，用于短信通知和身份验证
     * 对应数据库的phonenumber字段
     */
    // 手机号码，String类型
    private String phonenumber;

    /**
     * 用户性别
     * 用户性别信息，如男、女、未知
     * 对应数据库的sex字段
     */
    // 用户性别，String类型
    private String sex;

    /**
     * 用户头像
     * 用户头像文件ID，关联文件表
     * 对应数据库的avatar字段
     */
    // 用户头像，Long类型，存储文件ID
    private Long avatar;

    /**
     * 密码
     * 用户登录密码，使用BCrypt加密存储
     * 使用@TableField注解配置字段策略，确保密码不为空
     * insertStrategy=NOT_EMPTY：插入时密码不能为空
     * updateStrategy=NOT_EMPTY：更新时密码不能为空
     * whereStrategy=NOT_EMPTY：查询条件中密码不能为空
     * 对应数据库的password字段
     */
    // MyBatis-Plus注解：配置字段策略，确保密码字段不为空
    @TableField(
        insertStrategy = FieldStrategy.NOT_EMPTY,
        updateStrategy = FieldStrategy.NOT_EMPTY,
        whereStrategy = FieldStrategy.NOT_EMPTY
    )
    // 用户密码，String类型，加密存储
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     * 用户账号状态，控制账号是否可用
     * 0表示正常，1表示停用
     * 对应数据库的status字段
     */
    // 账号状态，String类型
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
     * 最后登录IP
     * 记录用户最后一次登录的IP地址
     * 用于安全审计和登录日志
     * 对应数据库的login_ip字段
     */
    // 最后登录IP，String类型
    private String loginIp;

    /**
     * 最后登录时间
     * 记录用户最后一次登录的时间
     * 用于判断账号活跃度和安全分析
     * 对应数据库的login_date字段
     */
    // 最后登录时间，Date类型
    private Date loginDate;

    /**
     * 备注
     * 用户备注信息，用于记录额外说明
     * 对应数据库的remark字段
     */
    // 备注，String类型
    private String remark;


    /**
     * 带参构造函数
     * 根据用户ID创建用户对象
     *
     * @param userId 用户ID
     */
    // 带参构造函数，接收用户ID参数
    public SysUser(Long userId) {
        // 初始化用户ID
        this.userId = userId;
    }

    /**
     * 判断是否为超级管理员
     * 通过比较用户ID是否等于系统常量中的超级管理员ID
     * 超级管理员拥有最高权限，不受数据权限限制
     *
     * @return 如果是超级管理员返回true，否则返回false
     */
    // 判断当前用户是否为超级管理员
    public boolean isSuperAdmin() {
        // 比较用户ID是否等于系统常量中的超级管理员ID
        return SystemConstants.SUPER_ADMIN_ID.equals(this.userId);
    }

}

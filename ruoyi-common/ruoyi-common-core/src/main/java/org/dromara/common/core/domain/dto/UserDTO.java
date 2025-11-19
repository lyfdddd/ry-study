package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户数据传输对象（DTO）
 * 用于在不同层之间传输用户数据，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class UserDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 主键，唯一标识一个用户
     */
    private Long userId;

    /**
     * 部门ID
     * 关联部门表，表示用户所属的部门
     */
    private Long deptId;

    /**
     * 用户账号
     * 登录系统的用户名，唯一
     */
    private String userName;

    /**
     * 用户昵称
     * 显示名称，用于前端展示
     */
    private String nickName;

    /**
     * 用户类型
     * 标识用户类型，如：sys_user（系统用户）、app_user（移动客户端用户）
     */
    private String userType;

    /**
     * 用户邮箱
     * 用户的电子邮箱地址
     */
    private String email;

    /**
     * 手机号码
     * 用户的手机号
     */
    private String phonenumber;

    /**
     * 用户性别
     * 0-男，1-女，2-未知
     */
    private String sex;

    /**
     * 帐号状态
     * 0-正常，1-停用，控制用户是否可以登录系统
     */
    private String status;

    /**
     * 创建时间
     * 用户账号的创建时间
     */
    private Date createTime;

}

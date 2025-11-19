package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色数据传输对象（DTO）
 * 用于在不同层之间传输角色数据，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class RoleDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     * 主键，唯一标识一个角色
     */
    private Long roleId;

    /**
     * 角色名称
     * 角色的显示名称，如：管理员、普通用户等
     */
    private String roleName;

    /**
     * 角色权限标识
     * 用于权限校验的唯一标识，如：admin、common等
     */
    private String roleKey;

    /**
     * 数据范围
     * 控制角色能访问的数据范围：
     * 1-全部数据权限
     * 2-自定义数据权限
     * 3-本部门数据权限
     * 4-本部门及以下数据权限
     * 5-仅本人数据权限
     * 6-部门及以下或本人数据权限
     */
    private String dataScope;

}

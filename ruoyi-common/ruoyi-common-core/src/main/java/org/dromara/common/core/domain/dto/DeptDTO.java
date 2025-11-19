package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门数据传输对象（DTO）
 * 用于在不同层之间传输部门数据，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class DeptDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     * 主键，唯一标识一个部门
     */
    private Long deptId;

    /**
     * 父部门ID
     * 用于构建部门树形结构，表示上级部门
     * 顶级部门的parentId为0
     */
    private Long parentId;

    /**
     * 部门名称
     * 部门的显示名称，如：研发部、市场部等
     */
    private String deptName;

}

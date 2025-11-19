package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 岗位数据传输对象（DTO）
 * 用于在不同层之间传输岗位数据，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class PostDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 岗位ID
     * 主键，唯一标识一个岗位
     */
    private Long postId;

    /**
     * 部门ID
     * 关联部门表，表示岗位所属的部门
     */
    private Long deptId;

    /**
     * 岗位编码
     * 岗位的唯一标识编码，用于权限校验等场景
     */
    private String postCode;

    /**
     * 岗位名称
     * 岗位的显示名称，如：Java开发工程师、产品经理等
     */
    private String postName;

    /**
     * 岗位类别编码
     * 岗位的分类标识，如：技术岗、管理岗等
     */
    private String postCategory;

}

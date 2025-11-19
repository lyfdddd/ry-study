package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典类型数据传输对象（DTO）
 * 用于在不同层之间传输字典类型数据，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class DictTypeDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典主键
     * 唯一标识一个字典类型
     */
    private Long dictId;

    /**
     * 字典名称
     * 字典类型的显示名称，如：用户状态、性别等
     */
    private String dictName;

    /**
     * 字典类型
     * 字典类型的唯一标识，用于关联字典数据，如：sys_user_status、sys_user_sex等
     */
    private String dictType;

    /**
     * 备注
     * 字典类型的说明信息
     */
    private String remark;

}

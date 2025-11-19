package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典数据数据传输对象（DTO）
 * 用于在不同层之间传输字典数据，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class DictDataDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典标签
     * 字典项的显示名称，用于前端展示
     */
    private String dictLabel;

    /**
     * 字典键值
     * 字典项的实际值，存储在数据库中
     */
    private String dictValue;

    /**
     * 是否默认
     * Y-是，N-否，表示该字典项是否为默认值
     */
    private String isDefault;

    /**
     * 备注
     * 字典项的说明信息
     */
    private String remark;

}

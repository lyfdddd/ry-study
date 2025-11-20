// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

// Jakarta验证注解：非空验证注解，用于参数校验
import jakarta.validation.constraints.NotNull;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 校验分组：新增分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.AddGroup;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;

/**
 * 流程变量参数业务对象
 * 用于封装流程实例的变量参数
 * 实现Serializable接口，支持序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowVariableBo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流程实例ID
     * 要设置变量的流程实例的唯一标识
     * 必填字段，新增时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "流程实例id为空", groups = AddGroup.class)
    private Long instanceId;

    /**
     * 流程变量Key
     * 变量的名称，用于在流程中引用该变量
     * 必填字段，新增时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "流程变量key为空", groups = AddGroup.class)
    private String key;

    /**
     * 流程变量Value
     * 变量的值，可以是字符串、数字、布尔值等
     * 必填字段，新增时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "流程变量value为空", groups = AddGroup.class)
    private String value;

}

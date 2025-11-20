// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

// Jakarta验证注解：非空验证注解，用于参数校验
import jakarta.validation.constraints.NotNull;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 校验分组：新增分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.AddGroup;
// 校验分组：编辑分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.EditGroup;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java集合类：List接口
import java.util.List;

/**
 * 任务操作业务对象
 * 用于描述任务委派、转办、加签、减签等操作的必要参数
 * 包含了用户ID、任务ID、任务相关的消息、以及加签/减签的用户ID列表
 * 实现Serializable接口，支持序列化传输
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class TaskOperationBo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 委派/转办人的用户ID
     * 用于指定任务要委派或转办给哪个用户
     * 必填字段，针对委派/转办操作时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "委派/转办人id不能为空", groups = {AddGroup.class})
    private String userId;

    /**
     * 加签/减签人的用户ID列表
     * 用于指定要加签或减签的用户列表
     * 必填字段，针对加签/减签操作时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "加签/减签id不能为空", groups = {EditGroup.class})
    private List<String> userIds;

    /**
     * 任务ID
     * 要操作的流程任务的唯一标识
     * 必填字段
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息
    @NotNull(message = "任务id不能为空")
    private Long taskId;

    /**
     * 意见或备注信息
     * 操作任务时填写的意见或备注
     * 可选字段
     */
    private String message;

}

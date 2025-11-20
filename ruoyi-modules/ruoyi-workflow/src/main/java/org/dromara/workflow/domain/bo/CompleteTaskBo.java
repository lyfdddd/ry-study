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
// Java集合类：HashMap实现类
import java.util.HashMap;
// Java集合类：List接口
import java.util.List;
// Java集合类：Map接口
import java.util.Map;
// Java工具类：Objects工具类
import java.util.Objects;

/**
 * 办理任务请求业务对象
 * 用于封装办理任务时需要的参数和数据
 * 实现Serializable接口，支持序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class CompleteTaskBo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     * 要办理的任务的唯一标识
     * 必填字段，新增时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "任务id不能为空", groups = {AddGroup.class})
    private Long taskId;

    /**
     * 附件ID
     * 任务办理时上传的附件ID，多个附件用逗号分隔
     * 用于记录任务办理的相关附件
     */
    private String fileId;

    /**
     * 抄送人员列表
     * 任务办理时需要抄送的人员列表
     * 用于消息通知和流程跟踪
     */
    private List<FlowCopyBo> flowCopyList;

    /**
     * 消息类型列表
     * 通知消息的类型，如"站内信"、"短信"、"邮件"等
     * 用于控制消息发送渠道
     */
    private List<String> messageType;

    /**
     * 办理意见
     * 任务办理时填写的意见或备注
     * 用于记录审批意见
     */
    private String message;

    /**
     * 消息通知内容
     * 发送给相关人员的通知消息内容
     * 用于流程通知
     */
    private String notice;

    /**
     * 办理人
     * 可选字段，用于覆盖当前节点的默认办理人
     * 如果为空，则使用流程定义中配置的办理人
     */
    private String handler;

    /**
     * 流程变量
     * 任务办理时传递的流程变量，用于影响流程走向
     * 如审批结果、审批意见等
     */
    private Map<String, Object> variables;

    /**
     * 弹窗选择的办理人
     * 用户在弹窗中选择的下一节点办理人
     * 用于动态指定任务办理人
     */
    private Map<String, Object> assigneeMap;

    /**
     * 扩展变量
     * 此处为逗号分隔的OSS文件ID，用于存储任务相关的文件
     * 用于扩展业务数据
     */
    private String ext;

    /**
     * 获取流程变量
     * 如果variables为null，则创建一个初始容量为16的HashMap
     * 过滤掉值为null的条目，确保变量集合中不包含空值
     * @return 处理后的流程变量Map
     */
    public Map<String, Object> getVariables() {
        // 如果variables为null，则创建一个初始容量为16的HashMap
        if (variables == null) {
            variables = new HashMap<>(16);
            return variables;
        }
        // 使用Stream API过滤掉值为null的条目，确保变量集合中不包含空值
        variables.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        // 返回处理后的variables
        return variables;
    }

}

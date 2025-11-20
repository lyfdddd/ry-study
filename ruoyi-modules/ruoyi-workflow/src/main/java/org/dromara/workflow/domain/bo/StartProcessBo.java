// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;


// Hutool工具类：对象工具类，提供对象判空、比较等操作
import cn.hutool.core.util.ObjectUtil;
// Jakarta验证注解：非空验证注解，用于参数校验
import jakarta.validation.constraints.NotBlank;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 校验分组：新增分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.AddGroup;
// 流程实例业务扩展实体：用于存储流程实例相关的业务扩展信息
import org.dromara.workflow.domain.FlowInstanceBizExt;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java集合类：HashMap实现类
import java.util.HashMap;
// Java集合类：Map接口
import java.util.Map;
// Java工具类：Objects工具类
import java.util.Objects;

/**
 * 启动流程业务对象
 * 用于封装启动流程时需要的参数和数据
 * 实现Serializable接口，支持序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class StartProcessBo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务唯一值ID
     * 关联具体业务数据的主键（如请假单ID、报销单ID）
     * 用于将流程实例与具体业务数据关联
     * 必填字段，新增时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotBlank(message = "业务ID不能为空", groups = {AddGroup.class})
    private String businessId;

    /**
     * 流程定义编码
     * 流程定义的唯一编码，如"leave_process"、"expense_process"
     * 用于指定要启动的流程定义
     * 必填字段，新增时必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotBlank(message = "流程定义编码不能为空", groups = {AddGroup.class})
    private String flowCode;

    /**
     * 办理人
     * 可选字段，用于覆盖当前节点的默认办理人
     * 如果为空，则使用流程定义中配置的办理人
     */
    private String handler;

    /**
     * 流程变量
     * 前端会提交一个Map，包含业务详情数据对象，如{'entity': {业务详情数据对象}}
     * 用于在流程执行过程中传递业务数据
     */
    private Map<String, Object> variables;

    /**
     * 流程业务扩展信息
     * 存储流程实例相关的业务扩展信息，如业务编码、业务标题等
     * 用于丰富流程实例的业务属性
     */
    private FlowInstanceBizExt bizExt;

    /**
     * 获取流程变量
     * 如果variables为null，则创建一个初始容量为16的HashMap
     * 过滤掉值为null的条目，确保变量集合中不包含空值
     * @return 处理后的流程变量Map
     */
    public Map<String, Object> getVariables() {
        // 如果variables为null，则创建一个初始容量为16的HashMap
        if (variables == null) {
            return new HashMap<>(16);
        }
        // 使用Stream API过滤掉值为null的条目，确保变量集合中不包含空值
        variables.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        // 返回处理后的variables
        return variables;
    }

    /**
     * 获取流程业务扩展信息
     * 如果bizExt为null，则创建一个新的FlowInstanceBizExt对象
     * 确保始终返回一个非null的bizExt对象
     * @return 流程业务扩展信息对象
     */
    public FlowInstanceBizExt getBizExt() {
        // 使用Hutool的ObjectUtil判断bizExt是否为null
        if (ObjectUtil.isNull(bizExt)) {
            // 如果为null，则创建一个新的FlowInstanceBizExt对象
            bizExt = new FlowInstanceBizExt();
        }
        // 返回bizExt对象
        return bizExt;
    }
}
